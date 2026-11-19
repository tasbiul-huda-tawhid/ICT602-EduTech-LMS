
import edutech.model.Credential;
import edutech.model.Learner;
import edutech.model.LearnerType;
import edutech.model.repository.InMemoryUserRepository;
import edutech.model.service.AuthService;
import edutech.model.service.PasswordHasher;
import edutech.view.AdminView;
import edutech.view.InputHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class KaiumAuthBoundaryAndAuth09Checks {
    private static int passed = 0;
    private static int failed = 0;
    private static final StringBuilder log = new StringBuilder();

    private static void check(String id, String description, boolean condition, String detail) {
        String status = condition ? "PASS" : "FAIL";
        log.append(id).append(" | ").append(status).append(" | ")
           .append(description).append(" | ").append(detail).append("\n");
        if (condition) passed++; else failed++;
    }

    private static Learner learner(String id, String name) {
        return new Learner(id, name, name.toLowerCase().replace(" ", ".") + "@example.test",
                           LearnerType.STUDENT);
    }

    public static void main(String[] args) throws Exception {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        AuthService auth = new AuthService(repo);

        // AUTH-07: minimum username boundary (2 rejected; 3 accepted)
        boolean username2Rejected = false;
        String username2Message = "";
        try {
            auth.registerUser(learner("T101", "Two Char User"), "ab", "ValidPass8");
        } catch (IllegalArgumentException ex) {
            username2Rejected = true;
            username2Message = ex.getMessage();
        }
        check("AUTH-07a", "Two-character username rejected",
              username2Rejected && username2Message.contains("at least 3"),
              "Input username length=2; observed='" + username2Message + "'");

        boolean username3Accepted = true;
        String username3Message = "registered";
        try {
            auth.registerUser(learner("T102", "Three Char User"), "abc", "ValidPass8");
        } catch (RuntimeException ex) {
            username3Accepted = false;
            username3Message = ex.getMessage();
        }
        check("AUTH-07b", "Three-character username accepted",
              username3Accepted,
              "Input username length=3; observed='" + username3Message + "'");

        // AUTH-07: minimum password boundary (7 rejected; 8 accepted)
        boolean password7Rejected = false;
        String password7Message = "";
        try {
            auth.registerUser(learner("T103", "Seven Char Pass"), "pass7", "Pass123");
        } catch (IllegalArgumentException ex) {
            password7Rejected = true;
            password7Message = ex.getMessage();
        }
        check("AUTH-07c", "Seven-character password rejected",
              password7Rejected && password7Message.contains("at least 8"),
              "Input password length=7; observed='" + password7Message + "'");

        boolean password8Accepted = true;
        String password8Message = "registered";
        try {
            auth.registerUser(learner("T104", "Eight Char Pass"), "pass8", "Pass1234");
        } catch (RuntimeException ex) {
            password8Accepted = false;
            password8Message = ex.getMessage();
        }
        check("AUTH-07d", "Eight-character password accepted",
              password8Accepted,
              "Input password length=8; observed='" + password8Message + "'");

        // A second account uses the same synthetic password to verify unique per-account salts.
        auth.registerUser(learner("T105", "Same Password User"), "samepass", "ValidPass8");

        // AUTH-09: inspect credential representation created by the actual AuthService.
        Credential c1 = repo.findCredentialByUsername("abc").orElseThrow();
        Credential c2 = repo.findCredentialByUsername("pass8").orElseThrow();
        Credential c3 = repo.findCredentialByUsername("samepass").orElseThrow();
        byte[] salt1 = c1.getSalt();
        byte[] salt2 = c2.getSalt();
        byte[] salt3 = c3.getSalt();
        byte[] hash1 = c1.getPasswordHash();
        byte[] hash2 = c2.getPasswordHash();
        byte[] hash3 = c3.getPasswordHash();

        check("AUTH-09a", "Salt length is 16 bytes",
              salt1.length == 16 && salt2.length == 16 && salt3.length == 16,
              "Observed salt lengths=" + salt1.length + ", " + salt2.length + ", " + salt3.length);

        check("AUTH-09b", "Derived hash length is 32 bytes (256 bits)",
              hash1.length == 32 && hash2.length == 32 && hash3.length == 32,
              "Observed hash lengths=" + hash1.length + ", " + hash2.length + ", " + hash3.length);

        check("AUTH-09c", "Same password on separate accounts receives different salts and hashes",
              !Arrays.equals(salt1, salt3) && !Arrays.equals(hash1, hash3),
              "Same test password used for two accounts; salts equal=" + Arrays.equals(salt1, salt3)
              + "; derived hashes equal=" + Arrays.equals(hash1, hash3));

        check("AUTH-09d", "Stored hash is not the plaintext password",
              !Arrays.equals(hash1, "ValidPass8".getBytes(StandardCharsets.UTF_8)),
              "Stored value is a 32-byte derived value; plaintext comparison=false");

        Set<String> fieldNames = Arrays.stream(Credential.class.getDeclaredFields())
                .map(Field::getName).collect(Collectors.toSet());
        boolean hasSaltAndHash = fieldNames.contains("salt") && fieldNames.contains("passwordHash");
        boolean noPlainPasswordField = !fieldNames.contains("password") && !fieldNames.contains("plainPassword");
        check("AUTH-09e", "Credential stores salt/hash fields and no plaintext-password field",
              hasSaltAndHash && noPlainPasswordField,
              "Credential fields=" + fieldNames);

        // Returned arrays must be defensive copies, not direct references to stored values.
        byte[] mutatedSaltCopy = c1.getSalt();
        mutatedSaltCopy[0] ^= 0x7F;
        byte[] mutatedHashCopy = c1.getPasswordHash();
        mutatedHashCopy[0] ^= 0x7F;
        check("AUTH-09f", "Credential getters return defensive copies",
              Arrays.equals(c1.getSalt(), salt1) && Arrays.equals(c1.getPasswordHash(), hash1),
              "Mutating returned arrays did not alter stored credential arrays");

        // AUTH-09: account listing must not disclose password, salt or hash.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        String listing;
        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            AdminView view = new AdminView(new InputHelper(new Scanner("")));
            view.showUsers(auth.getRegisteredUsers());
            System.out.flush();
            listing = bytes.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(original);
        }
        boolean listingHidesSecrets =
            !listing.contains("ValidPass8") &&
            !listing.contains("Pass1234") &&
            !listing.toLowerCase().contains(toHex(salt1).toLowerCase()) &&
            !listing.toLowerCase().contains(toHex(hash1).toLowerCase());
        check("AUTH-09g", "Administrator account listing omits password, salt and hash",
              listingHidesSecrets,
              "Captured listing:\n" + listing.trim());

        byte[] repeatHash = PasswordHasher.hash("ValidPass8", salt1);
        check("AUTH-09h", "Same password and same salt reproduce the same derived hash",
              Arrays.equals(hash1, repeatHash),
              "Derived hash comparison for same input/salt=" + Arrays.equals(hash1, repeatHash));

        log.append("SUMMARY | ").append(passed).append(" passed; ").append(failed)
           .append(" failed\n");
        System.out.print(log.toString());
        if (failed > 0) System.exit(1);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format("%02x", b));
        return out.toString();
    }
}
