package org.sessionbean.server.Beans;

import org.sessionbean.server.Entity.Emprunt;
import org.sessionbean.server.Entity.Media;
import org.sessionbean.server.exception.EmpruntException;
import org.sessionbean.server.service.MediathequeService;
import org.sessionbean.server.service.UtilisateurService;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@ManagedBean
@ViewScoped
public class EmpruntBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private MediathequeService mediathequeService;

    @EJB
    private UtilisateurService utilisateurService;

    private String searchTerm;
    private List<Media> searchResults;
    private List<Emprunt> empruntsActifs;
    private List<Emprunt> historiqueEmprunts;

    @ManagedProperty("#{sessionBean}")
    private SessionBean sessionBean;

    protected Long getCurrentUserId() {
        return sessionBean.getCurrentUserId();
    }

    @PostConstruct
    public void init() {
        Long userId = getCurrentUserId();
        empruntsActifs = mediathequeService.getEmpruntsActifs();
        historiqueEmprunts = utilisateurService.getHistoriqueEmprunts(userId);
    }

    public void searchMedia() {
        searchResults = mediathequeService.rechercherMedias(searchTerm);
    }

    public void emprunterMedia(Long mediaId) {
        try {
            mediathequeService.emprunterMedia(mediaId, getCurrentUserId());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Média emprunté avec succès"));
            init(); // Rafraîchir les listes
        } catch (EmpruntException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
        }
    }

    public void retournerMedia(Long mediaId) {
        try {
            mediathequeService.retournerMedia(mediaId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Média retourné avec succès"));
            init(); // Rafraîchir les listes
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors du retour"));
        }
    }

}