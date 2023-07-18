package com.jingrui.usercenter.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * 自定义Swagger 配置的配置类
 */
//表示这个类是一个配置类,会把这个类注入到ioc容器中
@Configuration
//开启swagger2的功能
@EnableSwagger2WebMvc
@Profile({"dev","test"}) //指定什么环境下可以开启接口
public class SwaggerConfig {
 
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //这里一定要标注你控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.jingrui.usercenter.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * api信息
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("用户中心")
                .description("接口文档")
                .termsOfServiceUrl("https://github.com/flyingheat")
                .contact(new Contact("jingrui","https://github.com/flyingheat","****@qq.com"))
                .version("1.0")
                .build();
    }
}