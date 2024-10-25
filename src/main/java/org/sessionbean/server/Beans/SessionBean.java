package org.sessionbean.server.Beans;

import org.sessionbean.server.Entity.Utilisateur;
import org.sessionbean.server.service.UtilisateurService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ManagedBean
@SessionScoped
public class SessionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(SessionBean.class.getName());

    @EJB
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateurConnecte;
    private String email;
    private String motDePasse;
    private Date derniereActivite;

    @PostConstruct
    public void init() {
        derniereActivite = new Date();
    }



    public String login() {
        try {
            utilisateurConnecte = utilisateurService.authentifier(email, motDePasse);
            if (utilisateurConnecte != null) {
                logger.info("Connexion réussie pour l'utilisateur: " + email);
                derniereActivite = new Date();
                motDePasse = null; // Clear the password after authentication
                return "/user/home?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Email ou mot de passe incorrect"));
                logger.warning("Tentative de connexion échouée pour l'email: " + email);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Erreur lors de la connexion: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la connexion"));
            return null;
        }
    }



    public String logout() {
        try {
            logger.info("Déconnexion de l'utilisateur: " + email);
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            return "/login?faces-redirect=true";
        } catch (Exception e) {
            logger.severe("Erreur lors de la déconnexion: " + e.getMessage());
            return null;
        }
    }

    public Long getCurrentUserId() {
        if (utilisateurConnecte == null) {
            redirect("/login");
            return null;
        }
        return utilisateurConnecte.getId();
    }

    private void redirect(String url) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + url);
        } catch (IOException e) {
            logger.severe("Erreur de redirection: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean isLoggedIn() {
        return utilisateurConnecte != null;
    }

    public boolean isSessionExpired() {
        if (derniereActivite == null) {
            return true;
        }
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(
                new Date().getTime() - derniereActivite.getTime());
        if (diffMinutes > 30) {
            logout();  // Automatically log out on session expiration
            return true;
        }
        return false;
    }

    public void updateLastActivity() {
        this.derniereActivite = new Date();
    }

    // Méthodes pour vérifier les informations de l'utilisateur
    public String getNomComplet() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom();
        }
        return "";
    }

    public String getRole() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getRole();
        }
        return "";
    }

    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(getRole());
    }
}
