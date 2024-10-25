package org.sessionbean.server;

import jakarta.persistence.PersistenceContext;
import org.sessionbean.server.Entity.Emprunt;
import org.sessionbean.server.Entity.Utilisateur;
import org.sessionbean.server.service.UtilisateurService;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
public class UtilisateurServiceImpl implements UtilisateurService {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Utilisateur authentifier(String email, String motDePasse) {
        // Simulated authentication logic (Replace with your actual data access logic)
        Utilisateur utilisateur = findUtilisateurByEmail(email); // Retrieve user by email
        if (utilisateur != null && utilisateur.getMotDePasse().equals(motDePasse)) {
            return utilisateur; // Successful authentication
        }
        return null; // Authentication failed
    }

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        // Vérification de l'unicité de l'email
        if (emailExiste(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        em.persist(utilisateur);
        return utilisateur;
    }

    @Override
    public void supprimerUtilisateur(Long id) {
        Utilisateur utilisateur = em.find(Utilisateur.class, id);
        if (utilisateur != null) {
            // Vérifier s'il n'y a pas d'emprunts en cours
            if (aDesEmpruntsEnCours(utilisateur)) {
                throw new IllegalStateException("L'utilisateur a des emprunts en cours");
            }
            em.remove(utilisateur);
        }
    }

    @Override
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur) {
        return em.merge(utilisateur);
    }

    @Override
    public Utilisateur trouverParId(Long id) {
        return em.find(Utilisateur.class, id);
    }

    @Override
    public List<Utilisateur> rechercherUtilisateurs(String nom) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.nom LIKE :nom OR u.prenom LIKE :nom", Utilisateur.class)
                .setParameter("nom", "%" + nom + "%")
                .getResultList();
    }

    @Override
    public List<Emprunt> getHistoriqueEmprunts(Long userId) {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.utilisateur.id = :userId ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    private boolean emailExiste(String email) {
        Long count = em.createQuery("SELECT COUNT(u) FROM Utilisateur u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    private boolean aDesEmpruntsEnCours(Utilisateur utilisateur) {
        Long count = em.createQuery("SELECT COUNT(e) FROM Emprunt e WHERE e.utilisateur = :utilisateur AND e.dateRetour IS NULL", Long.class)
                .setParameter("utilisateur", utilisateur)
                .getSingleResult();
        return count > 0;
    }

    private Utilisateur findUtilisateurByEmail(String email) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse("oooo"); // Placeholder for the password
        utilisateur.setNom("ouhba");
        utilisateur.setPrenom("youssef");
        utilisateur.setRole("ADMIN"); // Example role
        return utilisateur;
    }
}
