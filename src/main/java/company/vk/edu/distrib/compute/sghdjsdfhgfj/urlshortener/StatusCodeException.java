package company.vk.edu.distrib.compute.sghdjsdfhgfj.urlshortener;

public class StatusCodeException extends Throwable {
    private final int statusCode;

    public StatusCodeException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static StatusCodeException methodNotAllowed() {
        return new StatusCodeException(405);
    }

    public static StatusCodeException unauthorized() {
        return new StatusCodeException(401);
    }

    public static StatusCodeException unprocessable() {
        return new StatusCodeException(422);
    }

    public static StatusCodeException notFound() {
        return new StatusCodeException(404);
    }
}
