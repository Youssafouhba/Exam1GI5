package org.sessionbean.server;

import jakarta.persistence.EntityManager;
import org.sessionbean.server.Entity.Emprunt;
import org.sessionbean.server.Entity.Utilisateur;
import org.sessionbean.server.exception.EmpruntException;
import org.sessionbean.server.service.MediathequeService;
import org.sessionbean.server.Entity.Media;

import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
public class MediathequeServiceImpl implements MediathequeService {
    @PersistenceContext
    private EntityManager em;

    @Override
    public void ajouterMedia(Media media) {
        em.persist(media);
    }

    @Override
    public void supprimerMedia(Long id) {
        Media media = em.find(Media.class, id);
        if (media != null) {
            em.remove(media);
        }
    }

    @Override
    public List<Media> rechercherMedias(String titre) {
        return em.createQuery("SELECT m FROM Media m WHERE m.titre LIKE :titre", Media.class)
                .setParameter("titre", "%" + titre + "%")
                .getResultList();
    }

    @Override
    public void emprunterMedia(Long mediaId, Long utilisateurId) throws EmpruntException {

        Media media = em.find(Media.class, mediaId);
        if (media == null) {
            throw new EmpruntException("Le média demandé n'existe pas");
        }

        if (media.isEmprunte()) {
            throw new EmpruntException("Le média est déjà emprunté");
        }

        // Récupération de l'utilisateur
        Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
        if (utilisateur == null) {
            throw new EmpruntException("L'utilisateur n'existe pas");
        }

        // Vérification si l'utilisateur est actif
        if (!utilisateur.isActif()) {
            throw new EmpruntException("Le compte utilisateur n'est pas actif");
        }

        // Vérification du nombre d'emprunts en cours de l'utilisateur
        long nbEmpruntsEnCours = em.createQuery(
                        "SELECT COUNT(e) FROM Emprunt e WHERE e.utilisateur.id = :userId AND e.dateRetour IS NULL",
                        Long.class)
                .setParameter("userId", utilisateurId)
                .getSingleResult();

        if (nbEmpruntsEnCours >= 5) { // Limite arbitraire de 5 emprunts simultanés
            throw new EmpruntException("Nombre maximum d'emprunts atteint");
        }

        try {
            // Création de l'emprunt
            Emprunt emprunt = new Emprunt();
            emprunt.setMedia(media);
            emprunt.setUtilisateur(utilisateur);
            emprunt.setDateEmprunt(new Date());
            emprunt.setDateRetourPrevue(calculerDateRetour());

            // Mise à jour du statut du média
            media.setEmprunte(true);

            // Persistance de l'emprunt
            em.persist(emprunt);
            em.flush(); // Pour s'assurer que les modifications sont bien enregistrées
        } catch (Exception e) {
            throw new EmpruntException("Erreur lors de l'enregistrement de l'emprunt: " + e.getMessage());
        }
    }

    @Override
    public void retournerMedia(Long mediaId) {
        Media media = em.find(Media.class, mediaId);
        if (media != null) {
            Emprunt emprunt = em.createQuery("SELECT e FROM Emprunt e WHERE e.media.id = :mediaId AND e.dateRetour IS NULL", Emprunt.class)
                    .setParameter("mediaId", mediaId)
                    .getSingleResult();

            if (emprunt != null) {
                emprunt.setDateRetour(new Date());
                media.setEmprunte(false);
            }
        }
    }

    @Override
    public List<Emprunt> getEmpruntsActifs() {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.dateRetour IS NULL", Emprunt.class)
                .getResultList();
    }

    private Date calculerDateRetour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14); // Durée d'emprunt de 14 jours
        return cal.getTime();
    }
}
