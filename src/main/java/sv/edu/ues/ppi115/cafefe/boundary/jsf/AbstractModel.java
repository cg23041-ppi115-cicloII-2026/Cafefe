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

    // Métodos abstractos que implementa cada modelo hijo
    public abstract T instanciarRegistro();
    public abstract T getRegistroById(Object id);
    public abstract Object getIdByRegistro(T dato);

    // Conexión genérica con tu DefaultDAO
    public abstract DefaultDAO<T, ID> getDao();

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
    }

    /**
     * Botón "Editar" que está FUERA de la tabla: actúa sobre la fila
     * seleccionada (seleccion), no sobre registro.
     */
    public void btnEditarHandler() {
        if (this.seleccion != null) {
            this.seleccionarRegistro(this.seleccion);
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
    }

    public void btnModificarHandler() {
        if (this.registro != null && getDao() != null) {
            getDao().modificar(this.registro);
            this.cargarRegistros();
            this.limpiar();
        }
    }

    public void btnEliminarHandler() {
        if (this.registro != null && getDao() != null) {
            getDao().eliminar(this.registro);
            this.cargarRegistros();
            this.limpiar();
        }
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
}