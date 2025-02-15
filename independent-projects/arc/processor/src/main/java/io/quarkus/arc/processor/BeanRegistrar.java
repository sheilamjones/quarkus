package io.quarkus.arc.processor;

import java.util.Collection;

import jakarta.enterprise.inject.spi.InterceptionType;

import org.jboss.jandex.DotName;

/**
 * Allows a build-time extension to register synthetic beans.
 *
 * @author Martin Kouba
 */
public interface BeanRegistrar extends BuildExtension {

    /**
     *
     * @param context
     */
    void register(RegistrationContext context);

    interface RegistrationContext extends BuildContext {

        /**
         * Configure a new synthetic bean. The bean is not added to the deployment unless the {@link BeanConfigurator#done()}
         * method is called.
         *
         * @param beanClassName
         * @return a new synthetic bean configurator
         */
        <T> BeanConfigurator<T> configure(DotName beanClassName);

        /**
         * Configure a new synthetic bean. The bean is not added to the deployment unless the {@link BeanConfigurator#done()}
         * method is called.
         *
         * @param beanClass
         * @return a new synthetic bean configurator
         */
        default <T> BeanConfigurator<T> configure(Class<?> beanClass) {
            return configure(DotName.createSimple(beanClass.getName()));
        }

        /**
         * Configure a new synthetic interceptor. The interceptor is not added to the deployment unless the
         * {@link InterceptorConfigurator#creator(Class)} method is called.
         *
         * @param interceptionType
         * @return a new synthetic interceptor configurator
         */
        InterceptorConfigurator configureInterceptor(InterceptionType interceptionType);

        /**
         * The returned stream contains all non-synthetic beans (beans derived from classes) and beans
         * registered by other {@link BeanRegistrar}s before the stream is created.
         *
         * @return a new stream of beans
         */
        BeanStream beans();

        default Collection<InjectionPointInfo> getInjectionPoints() {
            return get(BuildExtension.Key.INJECTION_POINTS);
        }

        default InvokerFactory getInvokerFactory() {
            return get(BuildExtension.Key.INVOKER_FACTORY);
        }

    }

}
