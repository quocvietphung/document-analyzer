//package orgaplan.beratung.kreditunterlagen.config;
//
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtClaimNames;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.stereotype.Component;
//
//import java.util.Collection;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//@Component
//public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
//
//    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
//            new JwtGrantedAuthoritiesConverter();
//
//    @Override
//    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
//        System.out.println("Converting JWT to AuthenticationToken");
//        System.out.println("Access Token: " + jwt.getTokenValue());
//
//        Collection<GrantedAuthority> authorities = Stream.concat(
//                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
//                extractResourceRoles(jwt).stream()
//        ).collect(Collectors.toSet());
//
//        System.out.println("Authorities extracted: " + authorities);
//
//        JwtAuthenticationToken authToken = new JwtAuthenticationToken(
//                jwt,
//                authorities,
//                getPrincipleClaimName(jwt)
//        );
//
//        System.out.println("JWT Authentication Token created: " + authToken);
//
//        return authToken;
//    }
//
//    private String getPrincipleClaimName(Jwt jwt) {
//        String claimName = JwtClaimNames.SUB;
//        String principleName = jwt.getClaim(claimName);
//
//        System.out.println("Principal name extracted: " + principleName);
//
//        return principleName;
//    }
//
//    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
//        System.out.println("Extracting resource roles from JWT");
//
//        Map<String, Object> realmAccess;
//        Collection<String> realmRoles;
//
//        if (jwt.getClaim("realm_access") == null) {
//            System.out.println("No realm_access found in JWT");
//            // return empty collection
//            return Set.of();
//        }
//
//        realmAccess = jwt.getClaim("realm_access");
//        realmRoles = (Collection<String>) realmAccess.get("roles");
//
//        System.out.println("Roles found in realm_access: " + realmRoles);
//
//        Collection<? extends GrantedAuthority> authorities = realmRoles
//                .stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                .collect(Collectors.toSet());
//
//        System.out.println("Granted Authorities created from roles: " + authorities);
//        return authorities;
//    }
//}
