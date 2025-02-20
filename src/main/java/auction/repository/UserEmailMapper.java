//package auction.repository;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.factory.Mappers;
//import auction.dto.UserEmailPhotoDTO;
//import auction.model.CustomUserDetails;
//import auction.model.UserEmailImage;
//
//@Mapper
//public interface UserEmailMapper { // как это использовать?
//    UserEmailMapper INSTANCE = Mappers.getMapper(UserEmailMapper.class);
//
//    @Mapping(source = "user.username", target = "username")
//    @Mapping(source = "email.email", target = "email")
//    UserEmailPhotoDTO toDto(CustomUserDetails user, UserEmailImage email);
//}
