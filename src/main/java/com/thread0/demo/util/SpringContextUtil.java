package com.thread0.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 获取Spring的ApplicationContext对象工具，可以用静态方法的方式获取spring容器中的bean
 *
 * @author https://blog.csdn.net/chen_2890
 * @date 2019/6/26 16:20
 */
@Component
@Slf4j
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     */
    public static Object getBean(String name) {
        Object o = null;
        try {
            o = getApplicationContext().getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(e.getMessage());
        }
        return o;
    }

    /**
     * 通过class获取Bean.
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 通过name获取 Bean.
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项key
     */
    public static String getEnvironmentProperty(String key) {
        return getApplicationContext().getEnvironment().getProperty(key);
    }

    /**
     * 获取spring.profiles.active
     */
    public static String getActiveProfile() {
        return getApplicationContext().getEnvironment().getActiveProfiles()[0];
    }


    /**
     * 是否为指定环境
     *
     * @param profiles
     * @return
     */
    public static boolean isActiveProfile(String... profiles) {
        // 设置要显示swagger的环境
        Profiles of = Profiles.of(profiles);
        // 判断当前是否处于该环境
        return getApplicationContext().getEnvironment().acceptsProfiles(of);
    }

    /**
     * 是否为开发环境(本地,开发,测试)
     *
     * @return
     */
    public static boolean isDevProfile() {
        return isActiveProfile("local", "dev", "test");
    }

    /**
     * 是否正式环境(正式,预发布)
     *
     * @return
     */
    public static boolean isProdProfile() {
        return isActiveProfile("prod", "pre");
    }

    /**
     * 动态注册bean
     *
     * @param clazz
     */
    public static void registerBean(Class clazz) {

        String beanName = getBeanName(clazz);
        log.info("动态注册{}", beanName);
        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = getDefaultListableBeanFactory();
        //创建bean信息.
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        //动态注册bean.
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    /**
     * 获取BeanFactory
     * @return
     */
    public static DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
    }

    /**
     * 动态删除bean
     * @param clazz
     */
    public static void removeBean(Class clazz) {
        String beanName = getBeanName(clazz);
        log.info("动态删除{}", beanName);
        getDefaultListableBeanFactory().removeBeanDefinition(beanName);
    }

    /**
     * 取出类名,将首字母改成小写
     * @param cl
     * @return
     */
    private static String getBeanName(Class cl){
       return StringUtils.uncapitalize(cl.getSimpleName());
    }

}
