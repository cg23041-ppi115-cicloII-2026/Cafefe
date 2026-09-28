package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.ppi115.cafefe.control.RolRepository;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

/**
 * Converter para el p:pickList de roles de la vista Empleado_Rol.
 * Cada item se serializa como su UUID y se vuelve a cargar completo desde la
 * base de datos (mismo patron que el countryConverter del showcase de PrimeFaces).
 *
 * IMPORTANTE: el beans.xml usa bean-discovery-mode="annotated", y @Named NO es
 * una bean-defining annotation (solo es un @Qualifier). Sin un scope real la
 * clase no se descubre como bean de CDI: getConverter() devolvia null y el
 * pickList metia los UUID como String crudo (PropertyNotFoundException en
 * #{rol.nombre}). Por eso lleva @ApplicationScoped.
 *
 * @author 659684
 */
@Named("rolConverter")
@ApplicationScoped
@FacesConverter(value = "rolConverter", managed = true)
public class RolConverter implements Converter<Rol> {

    @Inject
    private RolRepository rolRepository;

    @Override
    public Rol getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return rolRepository.findById(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Conversion Error", "Rol no valido."));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Rol value) {
        return value == null ? null : String.valueOf(value.getIdRol());
    }
}
