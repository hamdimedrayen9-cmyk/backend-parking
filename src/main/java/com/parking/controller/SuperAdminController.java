package com.parking.controller;

import com.parking.entity.*;
import com.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired private ParkingService parkingService;
    @Autowired private UtilisateurService utilisateurService;
    @Autowired private ReservationService reservationService;
    @Autowired private ExportService exportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Parking> parkings = parkingService.findAll();
        List<Statistique> stats = parkingService.findAllStatistiques();
        double totalRevenus = stats.stream().mapToDouble(Statistique::getRevenus).sum();
        int totalReservations = stats.stream().mapToInt(Statistique::getNombreReservations).sum();

        model.addAttribute("parkings", parkings);
        model.addAttribute("totalRevenus", totalRevenus);
        model.addAttribute("totalReservations", totalReservations);
        model.addAttribute("totalAdmins", utilisateurService.findAllAdmins().size());
        model.addAttribute("totalClients", utilisateurService.findAllClients().size());
        
        // Données pour les graphiques du dashboard
        model.addAttribute("reservationsByDay", reservationService.getReservationsByDay());
        model.addAttribute("placesLibres", reservationService.countFreePlaces());
        model.addAttribute("placesOccupees", reservationService.countOccupiedPlaces());
        model.addAttribute("stats", stats);
        
        return "superadmin/dashboard";
    }

    @GetMapping("/carte")
    public String showCarte(@RequestParam(required = false) Long focusId, Model model) {
        List<Parking> parkings = parkingService.findAll();
        List<Map<String, Object>> parkingData = new ArrayList<>();
        
        for (Parking p : parkings) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("nom", p.getNom());
            map.put("adresse", p.getAdresse());
            map.put("latitude", p.getLatitude());
            map.put("longitude", p.getLongitude());
            map.put("capacite", p.getCapacite());
            long dispo = 0;
            try { dispo = (long) parkingService.findDisponiblesByParking(p).size(); } catch (Exception e) {}
            map.put("disponibles", dispo);
            parkingData.add(map);
        }
        
        model.addAttribute("parkings", parkings);
        model.addAttribute("parkingJson", parkingData);
        if (focusId != null) model.addAttribute("focusId", focusId);
        return "superadmin/carte";
    }

    // ===== PARKINGS =====
    @GetMapping("/parkings")
    public String listParkings(Model model) {
        model.addAttribute("parkings", parkingService.findAll());
        model.addAttribute("admins", utilisateurService.findAllAdmins());
        return "superadmin/parkings";
    }

    @GetMapping("/parkings/nouveau")
    public String formNouveauParking(Model model) {
        model.addAttribute("parking", new Parking());
        model.addAttribute("admins", utilisateurService.findAllAdmins());
        return "superadmin/parking-form";
    }

    @PostMapping("/parkings/sauvegarder")
    public String sauvegarderParking(@ModelAttribute Parking parking,
                                      @RequestParam(required = false) Long adminId,
                                      RedirectAttributes ra) {
        if (adminId != null) {
            utilisateurService.findAllAdmins().stream()
                .filter(a -> a.getId().equals(adminId))
                .findFirst()
                .ifPresent(parking::setAdminParking);
        }
        parkingService.save(parking);
        ra.addFlashAttribute("success", "Parking sauvegardé avec succès.");
        return "redirect:/superadmin/parkings";
    }

    @GetMapping("/parkings/modifier/{id}")
    public String formModifierParking(@PathVariable Long id, Model model) {
        parkingService.findById(id).ifPresent(p -> model.addAttribute("parking", p));
        model.addAttribute("admins", utilisateurService.findAllAdmins());
        return "superadmin/parking-form";
    }

    @GetMapping("/parkings/supprimer/{id}")
    public String supprimerParking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            parkingService.deleteById(id);
            ra.addFlashAttribute("success", "Parking supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer ce parking car il contient des places ou des réservations.");
        }
        return "redirect:/superadmin/parkings";
    }

    // ===== ADMINS =====
    @GetMapping("/admins")
    public String listAdmins(Model model) {
        model.addAttribute("admins", utilisateurService.findAllAdmins());
        model.addAttribute("parkings", parkingService.findAll());
        return "superadmin/admins";
    }

    @PostMapping("/admins/creer")
    public String creerAdmin(@RequestParam String nom,
                              @RequestParam String email,
                              @RequestParam String motDePasse,
                              @RequestParam(required = false) Long parkingId,
                              RedirectAttributes ra) {
        if (utilisateurService.existsByEmail(email)) {
            ra.addFlashAttribute("error", "Cet email est déjà utilisé.");
            return "redirect:/superadmin/admins";
        }
        AdminParking admin = utilisateurService.createAdmin(nom, email, motDePasse);
        if (parkingId != null) {
            parkingService.findById(parkingId).ifPresent(p -> {
                p.setAdminParking(admin);
                parkingService.save(p);
            });
        }
        ra.addFlashAttribute("success", "Admin créé avec succès.");
        return "redirect:/superadmin/admins";
    }

    @GetMapping("/admins/supprimer/{id}")
    public String supprimerAdmin(@PathVariable Long id, RedirectAttributes ra) {
        try {
            utilisateurService.findById(id).ifPresent(u -> {
                if (u instanceof AdminParking) {
                    AdminParking admin = (AdminParking) u;
                    // Désassigner le parking s'il y en a un
                    if (admin.getParking() != null) {
                        Parking p = admin.getParking();
                        p.setAdminParking(null);
                        parkingService.save(p);
                    }
                }
                utilisateurService.deleteById(id);
            });
            ra.addFlashAttribute("success", "Admin supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer cet admin.");
        }
        return "redirect:/superadmin/admins";
    }

    // ===== CLIENTS =====
    @GetMapping("/clients")
    public String listClients(Model model) {
        model.addAttribute("clients", utilisateurService.findAllClients());
        return "superadmin/clients";
    }

    // ===== STATISTIQUES =====
    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        List<Parking> parkings = parkingService.findAll();
        List<Statistique> stats = parkingService.findAllStatistiques();
        double totalRevenus = stats.stream().mapToDouble(Statistique::getRevenus).sum();
        int totalReservations = stats.stream().mapToInt(Statistique::getNombreReservations).sum();

        // Données pour les graphiques
        model.addAttribute("parkings", parkings);
        model.addAttribute("stats", stats);
        model.addAttribute("totalRevenus", totalRevenus);
        model.addAttribute("totalReservations", totalReservations);
        
        // Graphique: Réservations par jour
        model.addAttribute("reservationsByDay", reservationService.getReservationsByDay());
        
        // Graphique: Occupation
        long libres = reservationService.countFreePlaces();
        long occupees = reservationService.countOccupiedPlaces();
        model.addAttribute("placesLibres", libres);
        model.addAttribute("placesOccupees", occupees);
        
        return "superadmin/statistiques";
    }

    // ===== EXPORTS =====
    @GetMapping("/export/reservations/excel")
    public void exportReservationsExcel(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reservations_" + java.time.LocalDate.now() + ".xlsx";
        response.setHeader(headerKey, headerValue);
        exportService.exportReservationsToExcel(response, reservationService.findAll());
    }

    @GetMapping("/export/reservations/pdf")
    public void exportReservationsPdf(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reservations_" + java.time.LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);
        exportService.exportReservationsToPdf(response, reservationService.findAll());
    }

    @GetMapping("/export/statistiques/excel")
    public void exportStatsExcel(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=statistiques_" + java.time.LocalDate.now() + ".xlsx";
        response.setHeader(headerKey, headerValue);
        exportService.exportStatsToExcel(response, parkingService.findAllStatistiques());
    }

    @GetMapping("/export/statistiques/pdf")
    public void exportStatsPdf(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=statistiques_" + java.time.LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);
        exportService.exportStatsToPdf(response, parkingService.findAllStatistiques());
    }
}
