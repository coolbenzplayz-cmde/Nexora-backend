@Configuration
public class JwtConfig {

    @Value("${nexora.jwt.secret}")
    private String secret;

    @Value("${nexora.jwt.expiration}")
    private long expiration;

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }
}