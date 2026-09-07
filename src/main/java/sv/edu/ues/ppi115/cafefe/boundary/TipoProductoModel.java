package sv.edu.ues.ppi115.cafefe.boundary;

import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import sv.edu.ues.ppi115.cafefe.control.DAOInterface;
import sv.edu.ues.ppi115.cafefe.entity.TipoProducto;

@Named("tipoProductoModel")
@ViewScoped
public class TipoProductoModel implements Serializable {

    @Inject
    private DAOInterface<TipoProducto, Integer> dao;

    private TipoProducto registro;
    private List<TipoProducto> lista;

    @PostConstruct
    public void init() {
        this.limpiar();
        this.cargarLista();
    }

    public void cargarLista() {
        if (dao != null) {
            this.lista = dao.findAll();
        }
    }

    public void guardar() {
        if (this.registro != null && dao != null) {
            if (this.registro.getIdTipoProducto() == null) {
                dao.crear(this.registro); // Crea nuevo registro
            } else {
                dao.modificar(this.registro); // Actualiza si ya tiene ID
            }
            this.limpiar();
            this.cargarLista();
        }
    }

    public void seleccionar(TipoProducto seleccionado) {
        this.registro = seleccionado; // Carga el elemento en el formulario para editar
    }

    public void eliminar(TipoProducto seleccionado) {
        if (seleccionado != null && dao != null) {
            dao.eliminar(seleccionado);
            this.limpiar();
            this.cargarLista();
        }
    }

    public void limpiar() {
        this.registro = new TipoProducto();
    }

    public TipoProducto getRegistro() { return registro; }
    public void setRegistro(TipoProducto registro) { this.registro = registro; }
    public List<TipoProducto> getLista() { return lista; }
}