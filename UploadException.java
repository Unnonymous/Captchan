@SuppressWarnings("serial")
public class UploadException extends Exception {
}

@SuppressWarnings("serial")
class UnrecoverableUploadException extends UploadException {
}

//@SuppressWarnings("serial")
//class UnknownUploadException extends UnrecoverableUploadException {
//}

@SuppressWarnings("serial")
class Error404Exception extends UnrecoverableUploadException {
}

@SuppressWarnings("serial")
class BadCaptchaException extends UploadException {
}

@SuppressWarnings("serial")
class FileTooLargeException extends UploadException {
}

@SuppressWarnings("serial")
class FloodException extends UploadException {
}

@SuppressWarnings("serial")
class MaliciousFileException extends UploadException {
}

@SuppressWarnings("serial")
class MaxLimitException extends UnrecoverableUploadException {
}

@SuppressWarnings("serial")
class UploadFailException extends UploadException {
}
