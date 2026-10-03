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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.primefaces.event.TabChangeEvent;
import sv.edu.ues.ppi115.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.control.ProductoRepository;
import sv.edu.ues.ppi115.cafefe.control.TipoCaracteristicaRepository;
import sv.edu.ues.ppi115.cafefe.entity.Caracteristica;
import sv.edu.ues.ppi115.cafefe.entity.Producto;
import sv.edu.ues.ppi115.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.ppi115.cafefe.entity.TipoCaracteristica;

/**
 * Modelo de vista para el CRUD de ProductoCaracteristica.
 *
 * Ademas del CRUD generico de AbstractModel, maneja:
 *  - dos listas desplegables (productos y caracteristicas) para elegir
 *    las relaciones de la tabla intermedia;
 *  - filtros por producto, caracteristica y valor.
 */
@Named
@ViewScoped
public class ProductoCaracteristicaModel extends AbstractModel<ProductoCaracteristica, UUID> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tipado concreto de la tabla: AbstractModel declara getRegistros()
     * como List<T> y el analizador EL de NetBeans no sigue el tipo T,
     * por eso marcaba "propiedad desconocida" en cada #{item...} de la
     * vista. Con el tipo explicito aqui, el IDE infiere item como
     * ProductoCaracteristica. No cambia nada en tiempo de ejecucion.
     */
    @Override
    public List<ProductoCaracteristica> getRegistros() {
        return super.getRegistros();
    }

    @Override
    public ProductoCaracteristica getSeleccion() {
        return super.getSeleccion();
    }

    @Inject
    private ProductoCaracteristicaRepository productoCaracteristicaRepository;

    // Repositorios extra: solo para cargar las listas desplegables
    @Inject
    private ProductoRepository productoRepository;

    @Inject private CaracteristicaRepository caracteristicaRepository;

    @Inject
    private TipoCaracteristicaRepository tipoCaracteristicaRepository;

    // Selecciones de los desplegables (se guardan como UUID y se
    // convierten a las entidades Producto/Caracteristica al guardar)
    private UUID productoSeleccionado;
    private UUID caracteristicaSeleccionada;
    // tipoSeleccionado = primer nivel de la cascada de la ventana emergente
    private UUID tipoSeleccionado;

    // Listas de los desplegables (se cargan una sola vez por vista)
    private List<SelectItem> listaProductos;
    private List<SelectItem> listaCaracteristicas;
    private List<SelectItem> listaTipos;

    // Filtros: los UUID llegan como texto desde el formulario
    private String filtroProducto;
    private String filtroCaracteristica;
    private String filtroValor;

    public ProductoCaracteristicaModel() {
    }

    @Override
    public ProductoCaracteristica instanciarRegistro() {
        return new ProductoCaracteristica(UUID.randomUUID());
    }

    @Override
    public ProductoCaracteristica getRegistroById(Object id) {
        return productoCaracteristicaRepository.findById((UUID) id);
    }

    @Override
    public Object getIdByRegistro(ProductoCaracteristica dato) {
        return dato != null ? dato.getIdProductoCaracteristica() : null;
    }

    @Override
    public ProductoCaracteristicaRepository getDao() {
        return productoCaracteristicaRepository;
    }

    // ---------------------------------------------------------------
    // Ciclo del CRUD adaptado a las relaciones
    // ---------------------------------------------------------------
    @Override
    public void limpiar() {
        super.limpiar();
        // un registro nuevo no tiene relaciones elegidas todavia
        this.productoSeleccionado = null;
        this.caracteristicaSeleccionada = null;
        // una asignacion nueva empieza sin tipo de caracteristica elegido
        this.tipoSeleccionado = null;
    }

    @Override
    public void btnGuardarHandler() {
        if (this.registro != null) {
            // Validacion: la relacion necesita producto y caracteristica elegidos
            if (this.productoSeleccionado == null || this.caracteristicaSeleccionada == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Debe elegir el producto y la caracteristica", null));
                return; // no se guarda hasta que ambos combos tengan valor
            }
            // ---- Validacion contra la expresion regular del tipo ----
            // Sea cual sea la expresion guardada en tipo_caracteristica se
            // aplica aqui; si no existe o esta mal escrita, se detecta y
            // se bloquea el guardado con un aviso.
            Caracteristica elegida = caracteristicaRepository.findById(this.caracteristicaSeleccionada);
            TipoCaracteristica tipo = (elegida != null) ? elegida.getIdTipoCaracteristica() : null;
            String nombreTipo = (tipo != null && tipo.getNombre() != null)
                    ? tipo.getNombre() : "(sin tipo)";
            String regex = (tipo != null) ? tipo.getExpresionRegular() : null;
            String valor = this.registro.getValor();

            if (valor != null && !valor.trim().isEmpty()) {
                if (regex == null || regex.trim().isEmpty()) {
                    // DETECCION: el tipo de caracteristica no tiene expresion regular
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "El tipo de característica '" + nombreTipo
                                    + "' no tiene expresión regular definida", null));
                    return; // no se guarda sin poder validar
                }
                try {
                    if (!Pattern.matches(regex.trim(), valor.trim())) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_WARN,
                                "El valor no cumple la validación de " + nombreTipo
                                        + ": " + regex.trim(), null));
                        return; // el valor no cumple la expresion regular del tipo
                    }
                } catch (PatternSyntaxException e) {
                    // DETECCION: la expresion regular guardada esta mal escrita
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "La expresión regular del tipo '" + nombreTipo
                                    + "' no es válida: " + regex.trim(), null));
                    return; // no se puede validar con una expresion rota
                }
            }
            // Estado coherente: si la relacion ya existe en BD pero el estado
            // quedo en CREAR, guardarla como MODIFICAR para no intentar un
            // INSERT con ID duplicado.
            if (this.estado != ESTADO_CRUD.MODIFICAR
                    && this.registro.getIdProductoCaracteristica() != null
                    && productoCaracteristicaRepository.findById(this.registro.getIdProductoCaracteristica()) != null) {
                this.estado = ESTADO_CRUD.MODIFICAR;
            }
            // Convierte los UUID elegidos en las entidades reales
            this.registro.setIdProducto(productoRepository.findById(productoSeleccionado));
            this.registro.setIdCaracteristica(caracteristicaRepository.findById(caracteristicaSeleccionada));
        }
        try {
            super.btnGuardarHandler();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "No se pudo guardar: verifique los datos e intente de nuevo", null));
        }
    }

    @Override
    public void btnEditarHandler() {
        super.btnEditarHandler();
        // Al editar, los desplegables deben marcar lo que ya tiene el registro
        if (this.registro != null) {
            this.productoSeleccionado = registro.getIdProducto() != null
                    ? registro.getIdProducto().getIdProducto() : null;
            this.caracteristicaSeleccionada = registro.getIdCaracteristica() != null
                    ? registro.getIdCaracteristica().getIdCaracteristica() : null;
        }
    }

    /**
     * Eliminar con manejo de error: la base de datos tiene claves foraneas
     * ON DELETE RESTRICT, por lo que no deja borrar registros que tengan
     * datos relacionados. Se captura la excepcion para mostrar un mensaje
     * amigable en vez de fallar en silencio en el AJAX.
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
                    "No se pudo eliminar la característica del producto",
                    null));
        }
    }

    /**
     * Se dispara al cambiar de pestaña: vacía los desplegables para que
     * aparezcan los productos o características creados en la otra pestaña
     * de esta misma página.
     */
    public void onTabChange(TabChangeEvent evento) {
        this.listaProductos = null;
        this.listaCaracteristicas = null;
        this.listaTipos = null;
    }

    /**
     * Texto concatenado que muestra la tabla de la vista:
     * "tipo: valor caracteristica" (ejemplo: "volumen: 8.00 Onzas").
     */
    public String getResumen(ProductoCaracteristica pc) {
        if (pc == null) {
            return "";
        }
        Caracteristica car = pc.getIdCaracteristica();
        String tipo = (car != null && car.getIdTipoCaracteristica() != null
                && car.getIdTipoCaracteristica().getNombre() != null)
                ? car.getIdTipoCaracteristica().getNombre() : "";
        String nombre = (car != null && car.getNombre() != null) ? car.getNombre() : "";
        String valor = (pc.getValor() != null) ? pc.getValor() : "";
        return tipo + ": " + valor + " " + nombre;
    }

    // ---------------------------------------------------------------
    // Listas de los desplegables (carga perezosa: una vez por vista)
    // ---------------------------------------------------------------
    public List<SelectItem> getListaProductos() {
        if (this.listaProductos == null) {
            this.listaProductos = new ArrayList<>();
            for (Producto p : productoRepository.findAll()) {
                // inactivo: se muestra en gris pero no se puede elegir
                boolean inactivo = Boolean.FALSE.equals(p.getActivo());
                this.listaProductos.add(new SelectItem(p.getIdProducto(), p.getNombre(),
                        null, inactivo));
            }
        }
        return this.listaProductos;
    }

    /**
     * Tipos de caracteristica del primer select de la ventana emergente
     * (carga perezosa: una vez por vista). Los inactivos aparecen en
     * gris y no se pueden elegir.
     */
    public List<SelectItem> getListaTipos() {
        if (this.listaTipos == null) {
            this.listaTipos = new ArrayList<>();
            for (TipoCaracteristica t : tipoCaracteristicaRepository.findAll()) {
                boolean inactivo = Boolean.FALSE.equals(t.getActivo());
                this.listaTipos.add(new SelectItem(t.getIdTipoCaracteristica(),
                        t.getNombre(), null, inactivo));
            }
        }
        return this.listaTipos;
    }

    /**
     * Caracteristicas del tipo elegido (cascada: vacia hasta elegir el
     * tipo). Las inactivas se muestran en gris y no se pueden elegir.
     */
    public List<SelectItem> getListaCaracteristicas() {
        if (this.listaCaracteristicas == null) {
            this.listaCaracteristicas = new ArrayList<>();
            if (this.tipoSeleccionado != null) {
                TipoCaracteristica tipo = tipoCaracteristicaRepository.findById(this.tipoSeleccionado);
                UUID idTipo = (tipo != null) ? tipo.getIdTipoCaracteristica() : null;
                if (idTipo != null) {
                    for (Caracteristica c : caracteristicaRepository.findAll()) {
                        if (c.getIdTipoCaracteristica() != null
                                && idTipo.equals(c.getIdTipoCaracteristica().getIdTipoCaracteristica())) {
                            boolean inactivo = Boolean.FALSE.equals(c.getActivo());
                            this.listaCaracteristicas.add(new SelectItem(c.getIdCaracteristica(),
                                    c.getNombre(), null, inactivo));
                        }
                    }
                }
            }
        }
        return this.listaCaracteristicas;
    }

    public UUID getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    /**
     * Cambiar el tipo en la ventana emergente reinicia la seleccion de
     * caracteristica y recarga su lista (cascada Tipo > Caracteristica).
     */
    public void setTipoSeleccionado(UUID tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
        this.caracteristicaSeleccionada = null;
        this.listaCaracteristicas = null;
    }

    /**
     * Nombre de la caracteristica elegida que muestra el input del
     * formulario junto al boton de la ventana emergente.
     */
    public String getCaracteristicaElegida() {
        if (this.caracteristicaSeleccionada == null) {
            return "(ninguno)";
        }
        Caracteristica c = caracteristicaRepository.findById(this.caracteristicaSeleccionada);
        if (c == null) {
            return "(ninguno)";
        }
        return c.getNombre();
    }

    // ---------------------------------------------------------------
    // Filtros
    // ---------------------------------------------------------------
    public void buscarPorProducto() {
        UUID id = convertirUUID(filtroProducto);
        if (id != null) {
            this.registros = productoCaracteristicaRepository.findByProducto(id);
        } else {
            this.cargarRegistros(); // vacio o texto invalido: se muestra todo
        }
    }

    public void buscarPorCaracteristica() {
        UUID id = convertirUUID(filtroCaracteristica);
        if (id != null) {
            this.registros = productoCaracteristicaRepository.findByCaracteristica(id);
        } else {
            this.cargarRegistros();
        }
    }

    public void buscarPorValor() {
        if (this.filtroValor != null && !this.filtroValor.trim().isEmpty()) {
            this.registros = productoCaracteristicaRepository.findByValor(filtroValor.trim());
        } else {
            this.cargarRegistros();
        }
    }

    /**
     * Convierte el texto del formulario a UUID de forma segura;
     * devuelve null si el texto esta vacio o no es un UUID valido
     * (UUID.fromString lanza IllegalArgumentException si no lo es).
     */
    private UUID convertirUUID(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(texto.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------------
    public UUID getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(UUID productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public UUID getCaracteristicaSeleccionada() {
        return caracteristicaSeleccionada;
    }

    public void setCaracteristicaSeleccionada(UUID caracteristicaSeleccionada) {
        this.caracteristicaSeleccionada = caracteristicaSeleccionada;
    }

    public String getFiltroProducto() {
        return filtroProducto;
    }

    public void setFiltroProducto(String filtroProducto) {
        this.filtroProducto = filtroProducto;
    }

    public String getFiltroCaracteristica() {
        return filtroCaracteristica;
    }

    public void setFiltroCaracteristica(String filtroCaracteristica) {
        this.filtroCaracteristica = filtroCaracteristica;
    }

    public String getFiltroValor() {
        return filtroValor;
    }

    public void setFiltroValor(String filtroValor) {
        this.filtroValor = filtroValor;
    }
}
