// package xyz.brianuceda.sserafimflow.services;

// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// import xyz.brianuceda.sserafimflow.respositories.BankRepository;
// import xyz.brianuceda.sserafimflow.respositories.CompanyRepository;

// public class _CustomUserDetailsService implements UserDetailsService {
//   private final CompanyRepository companyRepository;
//   private final BankRepository bankRepository;

//   public _CustomUserDetailsService(CompanyRepository companyRepository, BankRepository bankRepository) {
//     this.companyRepository = companyRepository;
//     this.bankRepository = bankRepository;
//   }

//   @Override
//   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//     UserDetails userDetails = companyRepository.findByUsername(username)
//         .orElse(null);

//     if (userDetails == null) {
//       userDetails = bankRepository.findByUsername(username)
//           .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
//     }

//     return userDetails;
//   }
// }
