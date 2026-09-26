package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

/**
 * Repositorio del CRUD Caracteristica.
 *
 * Hereda de DefaultDAO crear/modificar/eliminar/findById/findAll.
 */
@Stateless
@LocalBean
public class CaracteristicaRepository extends DefaultDAO<Caracteristica, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public CaracteristicaRepository() {
        super(Caracteristica.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<Caracteristica> findByNombre(String nombre) {
        return getEntityManager()
                .createNamedQuery("Caracteristica.findByNombre", Caracteristica.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }

    public List<Caracteristica> findByActivo(Boolean activo) {
        return getEntityManager()
                .createNamedQuery("Caracteristica.findByActivo", Caracteristica.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<Caracteristica> findByObservaciones(String observaciones) {
        return getEntityManager()
                .createNamedQuery("Caracteristica.findByObservaciones", Caracteristica.class)
                .setParameter("observaciones", observaciones)
                .getResultList();
    }

    /**
     * Caracteristicas que pertenecen a un tipo de caracteristica dado.
     */
    public List<Caracteristica> findByTipoCaracteristica(UUID idTipoCaracteristica) {
        return getEntityManager()
                .createNamedQuery("Caracteristica.findByTipoCaracteristica", Caracteristica.class)
                .setParameter("idTipoCaracteristica", idTipoCaracteristica)
                .getResultList();
    }
}
