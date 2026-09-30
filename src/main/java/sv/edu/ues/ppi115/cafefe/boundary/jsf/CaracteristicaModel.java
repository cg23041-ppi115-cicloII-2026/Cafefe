package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.TabChangeEvent;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

/**
 * Modelo de vista para el CRUD Caracteristica.
 *
 * Hereda de AbstractModel el ciclo completo del CRUD
 * (nuevo, editar, guardar, eliminar, seleccionar fila) y agrega el
 * desplegable de tipos de caracteristica (obligatorio al guardar).
 */
@Named
@ViewScoped
public class CaracteristicaModel extends AbstractModel<Caracteristica, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tipado concreto de la tabla: el analizador EL de NetBeans no sigue
     * el tipo T de AbstractModel, por eso marcaba "propiedad desconocida"
     * en cada #{item...} de la vista. Con el tipo explicito aqui el IDE
     * infiere item como Caracteristica. Sin efecto en tiempo de ejecucion.
     */
    @Override
    public List<Caracteristica> getRegistros() {
        return super.getRegistros();
    }

    @Override
    public Caracteristica getSeleccion() {
        return super.getSeleccion();
    }

    @Inject
    private CaracteristicaRepository caracteristicaRepository;

    // Para el desplegable de tipos de caracteristica
    @Inject
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    // Tipo elegido en el desplegable (UUID; se convierte a entidad al guardar)
    private UUID tipoSeleccionado;

    // Lista del desplegable (se carga una sola vez por vista)
    private List<SelectItem> listaTipos;

    @Override
    public Caracteristica instanciarRegistro() {
        Caracteristica c = new Caracteristica(UUID.randomUUID());
        c.setActivo(Boolean.TRUE);
        c.setObservaciones("");
        return c;
    }

    @Override
    public Caracteristica getRegistroById(Object id) {
        return caracteristicaRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(Caracteristica dato) {
        return dato != null ? dato.getIdCaracteristica() : null;
    }

    @Override
    public CaracteristicaRepository getDao() {
        return caracteristicaRepository;
    }

    // ---------------------------------------------------------------
    // Desplegable de tipos de caracteristica
    // ---------------------------------------------------------------
    @Override
    public void limpiar() {
        super.limpiar();
        this.tipoSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler() {
        // Nombre obligatorio con un SOLO mensaje limpio en la parte superior:
        // el required del JSF lo duplicaba (arriba y junto al campo) y salia
        // con el clientId de por medio.
        if (this.registro != null
                && (this.registro.getNombre() == null || this.registro.getNombre().trim().isEmpty())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Debe ingresar el nombre", null));
            return; // no se guarda sin nombre y el formulario queda abierto
        }
        if (this.registro != null && this.tipoSeleccionado == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Debe elegir el tipo de caracteristica", null));
            return; // no se guarda sin tipo
        }
        // Estado coherente: si el registro apunta a una caracteristica que YA
        // existe pero el estado quedo en CREAR (p. ej. tras un eliminar
        // fallido), guardarlo como MODIFICAR para no intentar un INSERT con
        // ID duplicado.
        if (this.estado != ESTADO_CRUD.MODIFICAR
                && this.registro != null
                && this.registro.getIdCaracteristica() != null
                && caracteristicaRepository.findById(this.registro.getIdCaracteristica()) != null) {
            this.estado = ESTADO_CRUD.MODIFICAR;
        }
        try {
            if (this.registro != null) {
                this.registro.setIdTipoCaracteristica(
                        tipoCaracteristicaRepository.findById(tipoSeleccionado));
            }
            super.btnGuardarHandler(); // guarda, recarga la tabla y limpia
        } catch (Exception e) {
            // Sin esto la excepcion de EJB la traga MyFaces y NO se pinta nada
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
    }

    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        // Al editar, el combo debe marcar lo que ya tiene el registro
        if (this.registro != null && registro.getIdTipoCaracteristica() != null) {
            this.tipoSeleccionado = registro.getIdTipoCaracteristica().getIdTipoCaracteristica();
        } else {
            this.tipoSeleccionado = null;
        }
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT, por lo que no deja borrar una caracteristica que
     * este en uso por productos. Se captura la excepcion para mostrar un
     * mensaje amigable en vez de fallar en silencio en el AJAX.
     */
    @Override
    public void btnEliminarHandler() {
        try {
            super.btnEliminarHandler();
        } catch (Exception e) {
            // Estado limpio: no dejar el registro apuntando a la fila que no
            // se pudo borrar (evita un INSERT duplicado en el siguiente Guardar)
            this.limpiar();
            this.formVisible = false;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se puede eliminar: existen productos con esta caracteristica", null));
        }
    }

    /**
     * Se dispara al cambiar de pestaña: vacía el desplegable de tipos para
     * que aparezcan los tipos creados en la otra pestaña de esta misma página.
     */
    public void onTabChange(TabChangeEvent evento) {
        this.listaTipos = null;
    }

    /**
     * Lista desplegable de tipos de caracteristica (carga perezosa: una vez por vista).
     */
    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoCaracteristica t : tipoCaracteristicaRepository.findAll()) {
                if (Boolean.FALSE.equals(t.getActivo())) {
                    continue; // los inactivos no se pueden asignar
                }
                this.listaTipos.add(new SelectItem(t.getIdTipoCaracteristica(), t.getNombre()));
            }
        }
        return incluirActual(this.listaTipos, this.tipoSeleccionado, () -> {
            TipoCaracteristica t = tipoCaracteristicaRepository.findById(this.tipoSeleccionado);
            return t == null ? null : new SelectItem(t.getIdTipoCaracteristica(), t.getNombre());
        });
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }
}
