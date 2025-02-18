package xyz.sadiulhakim.util;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProperties {

    @Value("${default.pagination.size:0}")
    private int paginationSize;

    @Value("${default.user.image.folder:''}")
    private String userImageFolder;

    @Value("${default.user.image.name:''}")
    private String defaultUserPhotoName;
}
