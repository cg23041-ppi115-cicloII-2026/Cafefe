package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;

@Stateless
public class EmpleadoRolRepository extends DefaultDAO<EmpleadoRol, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public EmpleadoRolRepository() {
        super(EmpleadoRol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Asignaciones que pueden COBRAR: la asignacion activa y su rol
     * cobrador. Solo cajero, gerente y administrador pueden tomar y
     * cobrar ordenes (lista fija aqui en el codigo, sin campo en la BD).
     * Es la fuente del combo "Atendido por" de la vista Orden.
     */
    public List<EmpleadoRol> findCobradores() {
        return getEntityManager()
                .createQuery("SELECT er FROM EmpleadoRol er "
                        + "WHERE er.activo = true "
                        + "AND er.idEmpleado.activo = true "
                        + "AND er.idRol.activo = true "
                        + "AND LOWER(er.idRol.nombre) IN ('cajero', 'gerente', 'administrador') "
                        + "ORDER BY er.idEmpleado.nombre", EmpleadoRol.class)
                .getResultList();
    }
}
