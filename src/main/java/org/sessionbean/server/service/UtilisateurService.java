package org.sessionbean.server.service;

import org.sessionbean.server.Entity.Emprunt;
import org.sessionbean.server.Entity.Utilisateur;

import javax.ejb.Remote;
import java.util.List;

@Remote
public interface UtilisateurService {
    Utilisateur creerUtilisateur(Utilisateur utilisateur);
    Utilisateur authentifier(String email, String motDePasse);
    void supprimerUtilisateur(Long id);
    Utilisateur modifierUtilisateur(Utilisateur utilisateur);
    Utilisateur trouverParId(Long id);
    List<Utilisateur> rechercherUtilisateurs(String nom);
    List<Emprunt> getHistoriqueEmprunts(Long userId);
}
