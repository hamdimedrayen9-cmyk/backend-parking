package com.parking.controller;

import com.parking.entity.*;
import com.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminParkingController {

    @Autowired private ParkingService parkingService;
    @Autowired private ReservationService reservationService;
    @Autowired private UtilisateurService utilisateurService;
    @Autowired private ExportService exportService;

    private AdminParking getAdmin(Authentication auth) {
        return utilisateurService.findAdminByEmail(auth.getName()).orElseThrow();
    }

    private Parking getParking(Authentication auth) {
        AdminParking admin = getAdmin(auth);
        return parkingService.findByAdminParking(admin).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        try {
            Parking parking = getParking(auth);
            Statistique stat = parkingService.getOrCreateStatistique(parking);
            long placesDisponibles = parkingService.findPlacesByParking(parking)
                .stream().filter(Place::isDisponible).count();
            long reservationsEnAttente = reservationService.findByParkingIdAndStatut(parking.getId(), "EN_ATTENTE").size();

            model.addAttribute("parking", parking);
            model.addAttribute("stat", stat);
            model.addAttribute("placesDisponibles", placesDisponibles);
            model.addAttribute("reservationsEnAttente", reservationsEnAttente);
            model.addAttribute("reservations", reservationService.findByParkingId(parking.getId()));
        } catch (Exception e) {
            model.addAttribute("error", "Aucun parking ne vous est assigné.");
        }
        return "admin/dashboard";
    }

    // ===== PLACES =====
    @GetMapping("/places")
    public String places(Authentication auth, Model model) {
        Parking parking = getParking(auth);
        model.addAttribute("places", parkingService.findPlacesByParking(parking));
        model.addAttribute("parking", parking);
        return "admin/places";
    }

    @PostMapping("/places/ajouter")
    public String ajouterPlace(@RequestParam int numero, @RequestParam String type,
                                Authentication auth, RedirectAttributes ra) {
        Parking parking = getParking(auth);
        Place place = new Place();
        place.setNumero(numero);
        place.setType(type);
        place.setDisponible(true);
        place.setParking(parking);
        parkingService.savePlace(place);
        ra.addFlashAttribute("success", "Place ajoutée.");
        return "redirect:/admin/places";
    }

    @GetMapping("/places/supprimer/{id}")
    public String supprimerPlace(@PathVariable Long id, RedirectAttributes ra) {
        try {
            parkingService.deletePlaceById(id);
            ra.addFlashAttribute("success", "Place supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Impossible de supprimer cette place car elle est utilisée dans des réservations.");
        }
        return "redirect:/admin/places";
    }

    @PostMapping("/places/modifier/{id}")
    public String modifierPlace(@PathVariable Long id, @RequestParam int numero,
                                 @RequestParam String type, @RequestParam boolean disponible,
                                 RedirectAttributes ra) {
        parkingService.findPlaceById(id).ifPresent(p -> {
            p.setNumero(numero);
            p.setType(type);
            p.setDisponible(disponible);
            parkingService.savePlace(p);
        });
        ra.addFlashAttribute("success", "Place modifiée.");
        return "redirect:/admin/places";
    }

    // ===== TARIFS =====
    @GetMapping("/tarifs")
    public String tarifs(Authentication auth, Model model) {
        Parking parking = getParking(auth);
        model.addAttribute("tarifs", parkingService.findTarifsByParking(parking));
        model.addAttribute("parking", parking);
        return "admin/tarifs";
    }

    @PostMapping("/tarifs/ajouter")
    public String ajouterTarif(@RequestParam double prixHeure, @RequestParam String typeVehicule,
                                Authentication auth, RedirectAttributes ra) {
        Parking parking = getParking(auth);
        Tarif tarif = new Tarif();
        tarif.setPrixHeure(prixHeure);
        tarif.setTypeVehicule(typeVehicule);
        tarif.setParking(parking);
        parkingService.saveTarif(tarif);
        ra.addFlashAttribute("success", "Tarif ajouté.");
        return "redirect:/admin/tarifs";
    }

    @GetMapping("/tarifs/supprimer/{id}")
    public String supprimerTarif(@PathVariable Long id, RedirectAttributes ra) {
        try {
            parkingService.deleteTarifById(id);
            ra.addFlashAttribute("success", "Tarif supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression du tarif.");
        }
        return "redirect:/admin/tarifs";
    }

    // ===== RESERVATIONS =====
    @GetMapping("/reservations")
    public String reservations(Authentication auth, Model model) {
        Parking parking = getParking(auth);
        model.addAttribute("reservations", reservationService.findByParkingId(parking.getId()));
        model.addAttribute("parking", parking);
        return "admin/reservations";
    }

    @GetMapping("/reservations/valider/{id}")
    public String valider(@PathVariable Long id, RedirectAttributes ra) {
        reservationService.valider(id);
        ra.addFlashAttribute("success", "Réservation validée.");
        return "redirect:/admin/reservations";
    }

    @GetMapping("/reservations/refuser/{id}")
    public String refuser(@PathVariable Long id, RedirectAttributes ra) {
        reservationService.refuser(id);
        ra.addFlashAttribute("success", "Réservation refusée.");
        return "redirect:/admin/reservations";
    }

    @GetMapping("/reservations/supprimer/{id}")
    public String supprimer(@PathVariable Long id, Authentication auth, 
                            @RequestHeader(value = "Referer", required = false) String referer,
                            RedirectAttributes ra) {
        try {
            Parking parking = getParking(auth);
            reservationService.findById(id).ifPresentOrElse(r -> {
                if (r.getPlace().getParking().getId().equals(parking.getId())) {
                    reservationService.deleteById(id);
                    ra.addFlashAttribute("success", "Réservation supprimée.");
                } else {
                    ra.addFlashAttribute("error", "Action non autorisée.");
                }
            }, () -> ra.addFlashAttribute("error", "Réservation non trouvée."));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression.");
        }
        
        // Rediriger vers la page d'origine ou vers les réservations par défaut
        if (referer != null) {
            if (referer.contains("/admin/statistiques")) return "redirect:/admin/statistiques";
            if (referer.contains("/admin/dashboard")) return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/reservations";
    }

    // ===== STATISTIQUES =====
    @GetMapping("/statistiques")
    public String statistiques(Authentication auth, Model model) {
        Parking parking = getParking(auth);
        Statistique stat = parkingService.getOrCreateStatistique(parking);
        List<Reservation> reservations = reservationService.findByParkingId(parking.getId());
        
        model.addAttribute("parking", parking);
        model.addAttribute("stat", stat);
        model.addAttribute("reservations", reservations);
        
        // Graphique: Réservations par jour pour CE parking
        Map<String, Long> resByDay = reservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getDateDebut().toLocalDate().toString(),
                TreeMap::new,
                Collectors.counting()
            ));
        model.addAttribute("reservationsByDay", resByDay);
        
        // Graphique: Occupation locale
        long libres = parkingService.findPlacesByParking(parking).stream().filter(Place::isDisponible).count();
        long occupees = parkingService.findPlacesByParking(parking).stream().filter(p -> !p.isDisponible()).count();
        model.addAttribute("placesLibres", libres);
        model.addAttribute("placesOccupees", occupees);
        
        return "admin/statistiques";
    }

    // ===== EXPORTS =====
    @GetMapping("/export/reservations/excel")
    public void exportReservationsExcel(Authentication auth, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Parking parking = getParking(auth);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reservations_" + parking.getNom() + "_" + java.time.LocalDate.now() + ".xlsx";
        response.setHeader(headerKey, headerValue);
        exportService.exportReservationsToExcel(response, reservationService.findByParkingId(parking.getId()));
    }

    @GetMapping("/export/reservations/pdf")
    public void exportReservationsPdf(Authentication auth, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Parking parking = getParking(auth);
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reservations_" + parking.getNom() + "_" + java.time.LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);
        exportService.exportReservationsToPdf(response, reservationService.findByParkingId(parking.getId()));
    }

    @GetMapping("/export/statistiques/excel")
    public void exportStatsExcel(Authentication auth, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Parking parking = getParking(auth);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=statistiques_" + parking.getNom() + "_" + java.time.LocalDate.now() + ".xlsx";
        response.setHeader(headerKey, headerValue);
        exportService.exportStatsToExcel(response, List.of(parkingService.getOrCreateStatistique(parking)));
    }

    @GetMapping("/export/statistiques/pdf")
    public void exportStatsPdf(Authentication auth, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        Parking parking = getParking(auth);
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=statistiques_" + parking.getNom() + "_" + java.time.LocalDate.now() + ".pdf";
        response.setHeader(headerKey, headerValue);
        exportService.exportStatsToPdf(response, List.of(parkingService.getOrCreateStatistique(parking)));
    }
}
