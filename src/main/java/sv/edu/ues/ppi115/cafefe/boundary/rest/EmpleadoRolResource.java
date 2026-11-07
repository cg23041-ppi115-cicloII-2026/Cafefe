package sv.edu.ues.ppi115.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sv.edu.ues.ppi115.cafefe.control.EmpleadoRolService;
import sv.edu.ues.ppi115.cafefe.control.EmpleadoService;
import sv.edu.ues.ppi115.cafefe.control.RolService;
import sv.edu.ues.ppi115.cafefe.entity.Empleado;
import sv.edu.ues.ppi115.cafefe.entity.EmpleadoRol;
import sv.edu.ues.ppi115.cafefe.entity.Rol;

@Path("empleado/{id_empleado}/rol")
public class EmpleadoRolResource implements Serializable {

    @Inject
    EmpleadoRolService empleadoRolService;

    @Inject
    EmpleadoService empleadoService;

    @Inject
    RolService rolService;

    /**
     * POST /empleado/{id_empleado}/rol
     * Crea un rol asociado a un empleado en específico.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(
            @PathParam("id_empleado") UUID idEmpleado,
            EmpleadoRol empleadoRol,
            @Context UriInfo uriInfo) {
        if (empleadoRol == null || idEmpleado == null
                || empleadoRol.getIdEmpleadoRol() != null
                || empleadoRol.getIdRol() == null
                || empleadoRol.getIdRol().getIdRol() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(),
                            "El recurso no puede ser nulo, idEmpleado e idRol no pueden ser nulos y el recurso no debe tener un ID asignado")
                    .build();
        }
        try {
            Empleado empleado = empleadoService.findById(idEmpleado);
            if (empleado == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Empleado no encontrado")
                        .build();
            }

            Rol rol = rolService.findById(empleadoRol.getIdRol().getIdRol());
            if (rol == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Rol no encontrado")
                        .build();
            }
            empleadoRol.setIdEmpleadoRol(UUID.randomUUID());
            empleadoRol.setIdEmpleado(empleado);
            empleadoRol.setIdRol(rol);

            empleadoRolService.create(empleadoRol);

            UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(empleadoRol.getIdEmpleadoRol().toString());
            return Response.created(uriBuilder.build()).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /empleado/{id_empleado}/rol
     * Devuelve los roles asociados a un empleado en un rango paginado.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorRango(
            @PathParam("id_empleado") UUID idEmpleado,
            @QueryParam("first") @DefaultValue("0") int firstResult,
            @QueryParam("max") @DefaultValue("50") int maxResults) {
        if (idEmpleado == null || firstResult < 0 || maxResults <= 0 || maxResults > 50) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "Los parámetros 'first' debe ser >= 0 y 'max' debe ser > 0 y <= 50")
                    .build();
        }
        try {
            List<EmpleadoRol> registros = empleadoRolService.findByEmpleado(idEmpleado, firstResult, maxResults);
            Long total = empleadoRolService.contarPorEmpleado(idEmpleado);

            return Response.ok(registros)
                    .header(ResponseHeaders.TOTAL_REGISTROS.toString(), total)
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * GET /empleado/{id_empleado}/rol/{id}
     * Devuelve una asignación de rol específica de un empleado.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(
            @PathParam("id_empleado") UUID idEmpleado,
            @PathParam("id") UUID id) {
        if (id == null || idEmpleado == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idEmpleado e id no pueden ser nulos")
                    .build();
        }
        try {
            EmpleadoRol encontrado = empleadoRolService.findByIdYEmpleado(id, idEmpleado);
            if (encontrado != null && encontrado.getIdEmpleado() != null && idEmpleado.equals(encontrado.getIdEmpleado().getIdEmpleado())) {
                return Response.ok(encontrado).build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * PUT /empleado/{id_empleado}/rol/{id}
     * Actualiza una asignación de rol existente para el empleado.
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id_empleado") UUID idEmpleado,
            @PathParam("id") UUID id,
            EmpleadoRol empleadoRol) {
        if (empleadoRol == null || idEmpleado == null || id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "El recurso o los identificadores no pueden ser nulos")
                    .build();
        }
        try {
            EmpleadoRol existente = empleadoRolService.findByIdYEmpleado(id, idEmpleado);
            if (existente == null || existente.getIdEmpleado() == null || !idEmpleado.equals(existente.getIdEmpleado().getIdEmpleado())) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                        .build();
            }

            if (empleadoRol.getIdRol() == null
                    || empleadoRol.getIdRol().getIdRol() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idRol no puede ser nulo")
                        .build();
            }
            Rol rol = rolService.findById(empleadoRol.getIdRol().getIdRol());
            if (rol == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(ResponseHeaders.NOT_FOUND.toString(), "Rol no encontrado")
                        .build();
            }
            empleadoRol.setIdEmpleadoRol(id);
            empleadoRol.setIdEmpleado(existente.getIdEmpleado());
            empleadoRol.setIdRol(rol);

            empleadoRolService.update(empleadoRol);
            return Response.ok(empleadoRol).build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /empleado/{id_empleado}/rol/{id}
     * Elimina la asignación de rol de un empleado.
     */
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminar(
            @PathParam("id_empleado") UUID idEmpleado,
            @PathParam("id") UUID id) {
        if (id == null || idEmpleado == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header(ResponseHeaders.WRONG_PARAMETER.toString(), "idEmpleado e id no pueden ser nulos")
                    .build();
        }
        try {
            EmpleadoRol existente = empleadoRolService.findByIdYEmpleado(id, idEmpleado);
            if (existente != null && existente.getIdEmpleado() != null && idEmpleado.equals(existente.getIdEmpleado().getIdEmpleado())) {
                empleadoRolService.delete(existente);
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .header(ResponseHeaders.NOT_FOUND.toString(), "Recurso no encontrado")
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(ResponseHeaders.PROCESS_ERROR.toString(), ex.getMessage())
                    .build();
        }
    }
}