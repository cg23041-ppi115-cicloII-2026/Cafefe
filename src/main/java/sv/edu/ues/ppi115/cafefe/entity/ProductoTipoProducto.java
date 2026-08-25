/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.ppi115.cafefe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author 659684
 */
@Entity
@Table(name = "producto_tipo_producto")
@NamedQueries({
    @NamedQuery(name = "ProductoTipoProducto.findAll", query = "SELECT p FROM ProductoTipoProducto p"),
    @NamedQuery(name = "ProductoTipoProducto.findByFechaCreacion", query = "SELECT p FROM ProductoTipoProducto p WHERE p.fechaCreacion = :fechaCreacion"),
    @NamedQuery(name = "ProductoTipoProducto.findByObservaciones", query = "SELECT p FROM ProductoTipoProducto p WHERE p.observaciones = :observaciones")})
public class ProductoTipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ProductoTipoProductoPK productoTipoProductoPK;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Producto producto;
    @JoinColumn(name = "id_tipo_producto", referencedColumnName = "id_tipo_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoProducto idTipoProducto;

    public ProductoTipoProducto() {
    }

    public ProductoTipoProducto(ProductoTipoProductoPK productoTipoProductoPK) {
        this.productoTipoProductoPK = productoTipoProductoPK;
    }

    public ProductoTipoProducto(Object idProductoTipoProducto, Object idProducto) {
        this.productoTipoProductoPK = new ProductoTipoProductoPK(idProductoTipoProducto, idProducto);
    }

    public ProductoTipoProductoPK getProductoTipoProductoPK() {
        return productoTipoProductoPK;
    }

    public void setProductoTipoProductoPK(ProductoTipoProductoPK productoTipoProductoPK) {
        this.productoTipoProductoPK = productoTipoProductoPK;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoProducto getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(TipoProducto idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (productoTipoProductoPK != null ? productoTipoProductoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductoTipoProducto)) {
            return false;
        }
        ProductoTipoProducto other = (ProductoTipoProducto) object;
        if ((this.productoTipoProductoPK == null && other.productoTipoProductoPK != null) || (this.productoTipoProductoPK != null && !this.productoTipoProductoPK.equals(other.productoTipoProductoPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ppi115.cafefe.ProductoTipoProducto[ productoTipoProductoPK=" + productoTipoProductoPK + " ]";
    }
    
}
