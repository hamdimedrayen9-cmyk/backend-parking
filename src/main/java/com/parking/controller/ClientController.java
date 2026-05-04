package com.parking.controller;

import com.parking.entity.*;
import com.parking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private ParkingService parkingService;
    @Autowired private VehiculeService vehiculeService;
    @Autowired private ReservationService reservationService;

    private Client getClient(Authentication auth) {
        return utilisateurService.findClientByEmail(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Client client = getClient(auth);
        List<Reservation> reservations = reservationService.findByClient(client);
        long actives = reservations.stream()
            .filter(r -> r.getStatut().equals("EN_ATTENTE") || r.getStatut().equals("VALIDEE"))
            .count();
        model.addAttribute("client", client);
        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationsActives", actives);
        model.addAttribute("vehicules", vehiculeService.findByClient(client));
        return "client/dashboard";
    }

    @GetMapping("/carte")
    public String carte(Model model) {
        List<Parking> parkings = parkingService.findAll();
        
        // Simplifier les données pour JavaScript afin d'éviter les références circulaires
        List<Map<String, Object>> parkingData = parkings.stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("nom", p.getNom());
            map.put("adresse", p.getAdresse());
            map.put("latitude", p.getLatitude());
            map.put("longitude", p.getLongitude());
            map.put("disponibles", (long) parkingService.findDisponiblesByParking(p).size());
            return map;
        }).collect(Collectors.toList());
        
        model.addAttribute("parkings", parkings); // Garder pour le tableau Thymeleaf
        model.addAttribute("parkingJson", parkingData); // Utiliser pour la carte JS
        return "client/carte";
    }

    // ===== VÉHICULES =====
    @GetMapping("/vehicules")
    public String vehicules(Authentication auth, Model model) {
        Client client = getClient(auth);
        model.addAttribute("vehicules", vehiculeService.findByClient(client));
        return "client/vehicules";
    }

    @PostMapping("/vehicules/ajouter")
    public String ajouterVehicule(@RequestParam String immatriculation,
                                   @RequestParam String marque,
                                   @RequestParam String modele,
                                   @RequestParam String type,
                                   Authentication auth, RedirectAttributes ra) {
        if (vehiculeService.existsByImmatriculation(immatriculation)) {
            ra.addFlashAttribute("error", "Cette immatriculation existe déjà.");
            return "redirect:/client/vehicules";
        }
        Client client = getClient(auth);
        Vehicule v = new Vehicule();
        v.setImmatriculation(immatriculation.toUpperCase());
        v.setMarque(marque);
        v.setModele(modele);
        v.setType(type);
        v.setClient(client);
        vehiculeService.save(v);
        ra.addFlashAttribute("success", "Véhicule ajouté.");
        return "redirect:/client/vehicules";
    }

    @GetMapping("/vehicules/supprimer/{id}")
    public String supprimerVehicule(@PathVariable Long id, RedirectAttributes ra) {
        vehiculeService.deleteById(id);
        ra.addFlashAttribute("success", "Véhicule supprimé.");
        return "redirect:/client/vehicules";
    }

    // ===== RÉSERVATIONS =====
    @GetMapping("/reservations")
    public String reservations(Authentication auth, Model model) {
        Client client = getClient(auth);
        List<Reservation> all = reservationService.findByClient(client);
        
        List<Reservation> actives = all.stream()
            .filter(r -> r.getStatut().equals("EN_ATTENTE") || r.getStatut().equals("VALIDEE"))
            .toList();
            
        List<Reservation> terminees = all.stream()
            .filter(r -> r.getStatut().equals("TERMINEE"))
            .toList();
            
        List<Reservation> annulees = all.stream()
            .filter(r -> r.getStatut().equals("ANNULEE") || r.getStatut().equals("REFUSEE"))
            .toList();

        model.addAttribute("reservations", actives);
        model.addAttribute("historique", terminees);
        model.addAttribute("annulees", annulees);
        return "client/reservations";
    }

    @GetMapping("/reserver")
    public String formuReservation(@RequestParam(required = false) Long parkingId, Authentication auth, Model model) {
        Client client = getClient(auth);
        model.addAttribute("parkings", parkingService.findAll());
        model.addAttribute("vehicules", vehiculeService.findByClient(client));
        if (parkingId != null) {
            model.addAttribute("selectedParkingId", parkingId);
        }
        return "client/reserver";
    }

    @GetMapping("/reserver/places")
    public String getPlaces(@RequestParam Long parkingId, Model model) {
        parkingService.findById(parkingId).ifPresent(p -> {
            model.addAttribute("places", parkingService.findDisponiblesByParking(p));
            model.addAttribute("tarifs", parkingService.findTarifsByParking(p));
            model.addAttribute("parking", p);
        });
        return "client/places-disponibles :: placesFragment";
    }

    @PostMapping("/reservations/creer")
    public String creerReservation(@RequestParam Long placeId,
                                    @RequestParam Long vehiculeId,
                                    @RequestParam String dateDebut,
                                    @RequestParam String dateFin,
                                    Authentication auth, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vous devez être connecté pour effectuer une réservation.");
            return "redirect:/login";
        }
        try {
            Client client = getClient(auth);
            Place place = parkingService.findPlaceById(placeId).orElseThrow();
            
            // Vérifier si la place est toujours disponible
            if (!place.isDisponible()) {
                ra.addFlashAttribute("error", "Désolé, cette place vient d'être réservée. Veuillez en choisir une autre.");
                return "redirect:/client/reserver";
            }

            Vehicule vehicule = vehiculeService.findById(vehiculeId).orElseThrow();
            
            // Vérifier que le véhicule appartient bien au client
            if (!vehicule.getClient().getId().equals(client.getId())) {
                ra.addFlashAttribute("error", "Action non autorisée.");
                return "redirect:/client/vehicules";
            }

            LocalDateTime debut = LocalDateTime.parse(dateDebut);
            LocalDateTime fin = LocalDateTime.parse(dateFin);
            LocalDateTime now = LocalDateTime.now();

            if (debut.isBefore(now.minusMinutes(5))) {
                ra.addFlashAttribute("error", "La date de début ne peut pas être dans le passé.");
                return "redirect:/client/reserver";
            }

            if (fin.isBefore(debut) || fin.isEqual(debut)) {
                ra.addFlashAttribute("error", "La date de fin doit être après la date de début.");
                return "redirect:/client/reserver";
            }

            reservationService.creerReservation(client, place, vehicule, debut, fin);
            ra.addFlashAttribute("success", "Réservation créée avec succès ! En attente de validation.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la réservation: " + e.getMessage());
        }
        return "redirect:/client/reservations";
    }

    @GetMapping("/reservations/payer/{id}")
    public String payer(@PathVariable Long id, Authentication auth, Model model, RedirectAttributes ra) {
        Client client = getClient(auth);
        Reservation reservation = reservationService.findById(id).orElse(null);

        if (reservation != null && reservation.getClient().getId().equals(client.getId()) 
            && reservation.getStatut().equals("VALIDEE")) {
            model.addAttribute("reservation", reservation);
            return "client/paiement";
        }
        
        ra.addFlashAttribute("error", "Action non autorisée ou réservation non prête pour le paiement.");
        return "redirect:/client/reservations";
    }

    @GetMapping("/reservations/annuler/{id}")
    public String annuler(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Client client = getClient(auth);
        reservationService.findById(id).ifPresent(r -> {
            if (r.getClient().getId().equals(client.getId()) &&
                (r.getStatut().equals("EN_ATTENTE") || r.getStatut().equals("VALIDEE"))) {
                reservationService.annuler(id);
                ra.addFlashAttribute("success", "Réservation annulée.");
            }
        });
        return "redirect:/client/reservations";
    }
}
