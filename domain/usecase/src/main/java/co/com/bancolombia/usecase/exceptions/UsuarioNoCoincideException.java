package co.com.bancolombia.usecase.exceptions;

public class UsuarioNoCoincideException extends RuntimeException {
  public UsuarioNoCoincideException(String identityDocument) {
    super("El usuario con documento " + identityDocument
            + " no coincide con el JWT proporcionado. " +
            "Solo se permiten solicitudes a nombre propio.");
  }
}
