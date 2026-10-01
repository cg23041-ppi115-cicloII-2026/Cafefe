package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sv.edu.ues.ppi115.cafefe.control.DefaultDAO;

/**
 * Modelo abstracto genérico para controllers/beans de vistas JSF.
 * 
 * @param <T>  Tipo de la Entidad (ej: TipoProducto)
 * @param <ID> Tipo del ID/PK de la Entidad (ej: UUID)
 */
public abstract class AbstractModel<T, ID> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<T> registros;
    protected T registro;
    protected T seleccion; // fila seleccionada en la tabla (row select con AJAX)
    protected ESTADO_CRUD estado = ESTADO_CRUD.NINGUNO;
    // ¿Debe mostrarse el panel del formulario? La vista inicia SOLO con la
    // tabla y el form aparece cuando el usuario pulsa Nuevo o Editar.
    protected boolean formVisible = false;

    // Métodos abstractos que implementa cada modelo hijo
    public abstract T instanciarRegistro();
    public abstract T getRegistroById(Object id);
    public abstract Object getIdByRegistro(T dato);

    // Conexión genérica con tu DefaultDAO
    public abstract DefaultDAO<T, ID> getDao();

    // ---------------------------------------------------------------
    // Paginador lazy: contar() + cargar() para p:dataTable lazy="true"
    // ---------------------------------------------------------------
    private LazyModel<T> lazyModel;

    /**
     * Modelo lazy para la tabla: PrimeFaces pagina contra la BD
     * (DefaultDAO.findRange) y cuenta las filas (DefaultDAO.count),
     * en vez de cargar todos los registros en memoria.
     * Uso en la vista: value="#{xModel.lazyModel}" lazy="true" paginator="true"
     */
    public LazyModel<T> getLazyModel() {
        if (this.lazyModel == null) {
            this.lazyModel = new LazyModel<>(getDao(), this::getIdByRegistro);
        }
        return this.lazyModel;
    }

    @PostConstruct
    public void init() {
        this.cargarRegistros();
        this.limpiar();
    }

    public void cargarRegistros() {
        if (getDao() != null) {
            this.registros = getDao().findAll();
        } else {
            this.registros = new ArrayList<>();
        }
    }

    public void limpiar() {
        this.registro = this.instanciarRegistro();
        this.estado = ESTADO_CRUD.CREAR;
        this.seleccion = null; // al limpiar también se deselecciona la tabla
    }

    public void btnNuevoHandler() {
        this.limpiar();
        this.formVisible = true; // muestra el panel del formulario
    }

    /**
     * Botón "Editar" que está FUERA de la tabla: actúa sobre la fila
     * seleccionada (seleccion), no sobre registro.
     */
    public void btnEditarHandler() {
        if (this.seleccion != null) {
            this.seleccionarRegistro(this.seleccion);
            this.formVisible = true; // muestra el panel del formulario
        }
    }

    public void btnGuardarHandler() {
        if (this.registro == null || getDao() == null) {
            return;
        }
        if (this.estado == ESTADO_CRUD.MODIFICAR) {
            // Se está editando un registro existente (vino de "Editar")
            getDao().modificar(this.registro);
        } else {
            // Es un registro nuevo
            getDao().crear(this.registro);
        }
        this.cargarRegistros();
        this.limpiar();
        this.formVisible = false; // guardado: se vuelve a la vista de solo tabla
    }

    public void btnModificarHandler() {
        if (this.registro != null && getDao() != null) {
            getDao().modificar(this.registro);
            this.cargarRegistros();
            this.limpiar();
            this.formVisible = false;
        }
    }

    public void btnEliminarHandler() {
        if (this.registro != null && getDao() != null) {
            getDao().eliminar(this.registro);
            this.cargarRegistros();
            this.limpiar();
            this.formVisible = false;
        }
    }

    /**
     * Botón "Cancelar" del formulario: limpia y regresa a la vista de solo tabla.
     */
    public void btnCancelarHandler() {
        this.limpiar();
        this.formVisible = false;
    }

    public void seleccionarRegistro(T r) {
        this.registro = r;
        this.estado = ESTADO_CRUD.MODIFICAR;
    }

    // Getters y Setters
    public List<T> getRegistros() {
        return registros;
    }

    public void setRegistros(List<T> registros) {
        this.registros = registros;
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public T getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(T seleccion) {
        this.seleccion = seleccion;
    }

    public ESTADO_CRUD getEstado() {
        return estado;
    }

    public void setEstado(ESTADO_CRUD estado) {
        this.estado = estado;
    }

    /**
     * ¿El formulario esta en modo edicion (vino de "Editar") y no de
     * "Nuevo"? La vista lo usa en disabled="#{xModel.editando}" para los
     * calendarios: las fechas solo se eligen al crear. Las listas de
     * seleccion NO se apagan en edicion; ahi el gris lo define el campo
     * activo del registro (inactivos en gris y no elegibles).
     */
    public boolean isEditando() {
        return this.estado == ESTADO_CRUD.MODIFICAR;
    }

    public boolean isFormVisible() {
        return formVisible;
    }

    public void setFormVisible(boolean formVisible) {
        this.formVisible = formVisible;
    }
}