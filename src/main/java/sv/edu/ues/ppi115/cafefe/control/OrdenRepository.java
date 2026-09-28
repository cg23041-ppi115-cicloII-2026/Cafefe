package sv.edu.ues.ppi115.cafefe.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.entity.Orden;
import sv.edu.ues.ppi115.cafefe.entity.OrdenProducto;

@Stateless
@LocalBean
public class OrdenRepository extends DefaultDAO<Orden, UUID> {

    @PersistenceContext(unitName = "cafefe-PU")
    private EntityManager em;

    public OrdenRepository() {
        super(Orden.class);
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * Crea la orden y TODAS sus lineas en UNA sola transaccion (metodo
     * de un @Stateless): o se guarda todo o no se guarda nada. La orden
     * queda cerrada a partir de aqui y ya no se edita.
     */
    public void crearConLineas(Orden orden, List<OrdenProducto> lineas) {
        if (orden == null || orden.getIdOrden() == null) {
            return;
        }
        getEntityManager().persist(orden);
        if (lineas != null) {
            for (OrdenProducto linea : lineas) {
                linea.setIdOrden(orden);
                getEntityManager().persist(linea);
            }
        }
    }

    /**
     * Elimina una Orden: PRIMERO sus lineas y luego la orden, en UNA
     * sola transaccion (@Stateless). Si alguna linea ya fue facturada,
     * la FK de factura_orden_producto bloquea el borrado de lineas y se
     * revierte todo, dejando la orden intacta. Luego re-atacha la orden
     * al EntityManager actual para evitar "Removing a detached instance"
     * cuando el objeto viene desde el @ViewScoped.
     */
    @Override
    public void eliminar(Orden registro) {
        if (registro == null || registro.getIdOrden() == null) {
            return;
        }
        getEntityManager().createQuery(
                "DELETE FROM OrdenProducto o WHERE o.idOrden.idOrden = :id")
                .setParameter("id", registro.getIdOrden())
                .executeUpdate();

        Orden managed = getEntityManager().contains(registro)
                ? registro
                : getEntityManager().find(Orden.class, registro.getIdOrden());

        if (managed != null) {
            getEntityManager().remove(managed);
        }
    }
}

