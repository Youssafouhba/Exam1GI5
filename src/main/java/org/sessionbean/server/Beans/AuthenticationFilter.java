package org.sessionbean.server.Beans;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/user/*")
public class AuthenticationFilter implements Filter {
    private FilterConfig filterConfig = null;
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        logger.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            // Obtenir le contexte de l'application
            String contextPath = req.getContextPath();

            // Obtenir l'URL demandée
            String requestUrl = req.getRequestURL().toString();

            // Vérifier si c'est une ressource statique (CSS, JS, images)
            if (requestUrl.matches(".*(css|jpg|png|gif|js|jpeg)")) {
                chain.doFilter(request, response);
                return;
            }

            // Récupérer la session
            HttpSession session = req.getSession(false);

            // Vérifier si l'utilisateur est connecté
            SessionBean sessionBean = session != null ?
                    (SessionBean) session.getAttribute("sessionBean") : null;

            boolean isLoggedIn = sessionBean != null &&
                    sessionBean.getUtilisateurConnecte() != null;

            // Si l'utilisateur n'est pas connecté, rediriger vers la page de login
            if (!isLoggedIn) {
                logger.warning("Tentative d'accès non autorisé à : " + requestUrl);
                res.sendRedirect(contextPath + "/login.xhtml");
                return;
            }

            // Vérifier si la session n'a pas expiré
            if (session != null) {
                // Actualiser le timestamp de dernière activité
                session.setAttribute("lastActivity", System.currentTimeMillis());
            }

            // Si tout est OK, continuer la chaîne de filtres
            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.severe("Erreur dans AuthenticationFilter: " + e.getMessage());
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
        logger.info("AuthenticationFilter destroyed");
    }

    // Méthodes utilitaires
    private boolean isResourceRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/javax.faces.resource/");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private void handleSessionTimeout(HttpServletRequest request,
                                      HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            response.setContentType("text/xml");
            response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<partial-response><redirect url=\""
                    + request.getContextPath()
                    + "/login.xhtml?session=timeout\"/></partial-response>");
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/login.xhtml?session=timeout");
        }
    }
}
