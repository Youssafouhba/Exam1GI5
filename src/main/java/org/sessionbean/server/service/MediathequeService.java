package org.sessionbean.server.service;

import org.sessionbean.server.Entity.Emprunt;

import javax.ejb.Remote;
import org.sessionbean.server.Entity.Media;
import org.sessionbean.server.exception.EmpruntException;

import java.util.List;

@Remote
public interface MediathequeService {
    void ajouterMedia(Media media);
    void supprimerMedia(Long id);
    List<Media> rechercherMedias(String titre);
    void emprunterMedia(Long mediaId, Long utilisateurId) throws EmpruntException;
    void retournerMedia(Long mediaId);
    List<Emprunt> getEmpruntsActifs();
}
