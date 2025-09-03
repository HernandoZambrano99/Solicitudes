package co.com.bancolombia.r2dbc;


import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Long,
        ReactiveUserRepository
        > {

    public UserRepositoryAdapter(ReactiveUserRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, null);
    }

    public Mono<User> findByDocument(String identityDocument) {
        return super.repository.findByIdentityDocument(identityDocument)
                .map(userEntity -> super.mapper.map(userEntity, User.class));
    }


}
