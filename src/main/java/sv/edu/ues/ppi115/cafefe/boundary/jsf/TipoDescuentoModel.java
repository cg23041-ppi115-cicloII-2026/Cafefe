package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import sv.edu.ues.ppi115.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.ppi115.cafefe.entity.TipoDescuento;

@Named("tipoDescuentoModel")
@ViewScoped
public class TipoDescuentoModel implements Serializable {

    @Inject
    private TipoDescuentoDAO dao;

    private TipoDescuento registro;
    private List<TipoDescuento> lista;

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
            if (this.registro.getIdTipoDescuento() == null) {
                dao.crear(this.registro); // Crea nuevo registro
            } else {
                dao.modificar(this.registro); // Actualiza si ya tiene ID
            }
            this.limpiar();
            this.cargarLista();
        }
    }

    public void seleccionar(TipoDescuento seleccionado) {
        this.registro = seleccionado; // Carga el registro en el formulario para editar
    }

    public void eliminar(TipoDescuento seleccionado) {
        if (seleccionado != null && dao != null) {
            dao.eliminar(seleccionado);
            this.limpiar();
            this.cargarLista();
        }
    }

    public void limpiar() {
        this.registro = new TipoDescuento();
    }

    public TipoDescuento getRegistro() { return registro; }
    public void setRegistro(TipoDescuento registro) { this.registro = registro; }
    public List<TipoDescuento> getLista() { return lista; }
}