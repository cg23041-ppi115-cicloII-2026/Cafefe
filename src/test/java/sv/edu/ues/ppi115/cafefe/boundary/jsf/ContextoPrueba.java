package sv.edu.ues.ppi115.cafefe.boundary.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ContextoPrueba extends FacesContext {

    public final List<FacesMessage> mensajes = new ArrayList<>();

    @Override
    public void addMessage(String clientId, FacesMessage message) {
        this.mensajes.add(message);
    }

    @Override
    public Iterator<FacesMessage> getMessages() {
        return mensajes.iterator();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        return mensajes.iterator();
    }

    @Override
    public FacesMessage.Severity getMaximumSeverity() {
        FacesMessage.Severity max = null;
        for (FacesMessage m : mensajes) {
            if (m.getSeverity() != null
                    && (max == null || m.getSeverity().getOrdinal() > max.getOrdinal())) {
                max = m.getSeverity();
            }
        }
        return max;
    }

    @Override
    public Iterator<String> getClientIdsWithMessages() {
        return Collections.emptyIterator();
    }

    @Override
    public List<FacesMessage> getMessageList() {
        return mensajes;
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId) {
        return mensajes;
    }

    @Override public Application getApplication() { return null; }
    @Override public Lifecycle getLifecycle() { return null; }
    @Override public ExternalContext getExternalContext() { return null; }
    @Override public RenderKit getRenderKit() { return null; }
    @Override public boolean getRenderResponse() { return false; }
    @Override public boolean getResponseComplete() { return false; }
    @Override public ResponseStream getResponseStream() { return null; }
    @Override public void setResponseStream(ResponseStream stream) { }
    @Override public ResponseWriter getResponseWriter() { return null; }
    @Override public void setResponseWriter(ResponseWriter writer) { }
    @Override public UIViewRoot getViewRoot() { return null; }
    @Override public void setViewRoot(UIViewRoot root) { }
    @Override public void release() { }
    @Override public void renderResponse() { }
    @Override public void responseComplete() { }

    public static void colocarComoActual(FacesContext ctx) {
        try {
            Method m = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
            m.setAccessible(true);
            m.invoke(null, ctx);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo colocar el FacesContext de prueba", e);
        }
    }
}
