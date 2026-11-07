package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;

@Stateless
@LocalBean
public class CaracteristicaService extends AbstractDataAccess<Caracteristica> {

    @PersistenceContext(unitName = "CafefePU")
    EntityManager em;

    public CaracteristicaService() {
        super(Caracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Busca características por tipo en un rango paginado.
     * @param idTipoCaracteristica identificador del tipo de característica
     * @param first índice inicial del rango
     * @param max cantidad máxima de registros
     * @return lista de características encontradas
     * @throws IllegalArgumentException si algún parámetro es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public List<Caracteristica> findByTipoCaracteristica(UUID idTipoCaracteristica, int first, int max) {
        if (idTipoCaracteristica != null && first >= 0 && max > 0) {
            try {
                if (em != null) {
                    return em.createNamedQuery("Caracteristica.findByTipoCaracteristica", Caracteristica.class)
                            .setParameter("idTipoCaracteristica", idTipoCaracteristica)
                            .setFirstResult(first)
                            .setMaxResults(max)
                            .getResultList();
                }
                throw new NullPointerException("El repositorio es nulo");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalArgumentException("Parámetros inválidos");
    }

    /**
     * Cuenta características por tipo.
     * @param idTipoCaracteristica identificador del tipo de característica
     * @return total de características encontradas
     * @throws IllegalArgumentException si el identificador es inválido
     * @throws IllegalStateException si ocurre un error al consultar
     */
    public long contarPorTipoCaracteristica(UUID idTipoCaracteristica) {
        if (idTipoCaracteristica == null) {
            throw new IllegalArgumentException("Parámetros inválidos");
        }
        try {
            if (em != null) {
                return em.createNamedQuery("Caracteristica.contarPorTipoCaracteristica", Long.class)
                        .setParameter("idTipoCaracteristica", idTipoCaracteristica)
                        .getSingleResult();
            }
            throw new NullPointerException("El repositorio es nulo");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}