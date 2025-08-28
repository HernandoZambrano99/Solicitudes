package co.com.bancolombia.usecase.exceptions;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String idUsuario) {
        super("El usuario con documento de identidad " + idUsuario + " no se encuentra registrado en la base de datos");
    }
}
