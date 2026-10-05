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
     * Asignaciones que pueden REALIZAR ordenes: los empleados con rol
     * cajero, gerente, administrador o mesero (lista fija aqui en el
     * codigo, sin campo en la BD). Devuelve tambien las inactivas: el
     * combo las muestra en gris, sin poder elegirlas.
     * Es la fuente del combo "Atendido por" de la vista Orden.
     */
    public List<EmpleadoRol> findCobradores() {
        return getEntityManager()
                .createQuery("SELECT er FROM EmpleadoRol er "
                        + "WHERE LOWER(er.idRol.nombre) IN ('cajero', 'gerente', 'administrador', 'mesero', 'camarero') "
                        + "ORDER BY er.idEmpleado.nombre", EmpleadoRol.class)
                .getResultList();
    }
}
