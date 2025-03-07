package io.quarkus.deployment.builditem;

import java.util.Objects;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.quarkus.bootstrap.app.DependencyInfoProvider;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.model.PlatformImports;
import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.deployment.BootstrapConfig;

public final class AppModelProviderBuildItem extends SimpleBuildItem {

    private static final Logger log = Logger.getLogger(AppModelProviderBuildItem.class);

    private final ApplicationModel appModel;
    private final Supplier<DependencyInfoProvider> depInfoProvider;

    public AppModelProviderBuildItem(ApplicationModel appModel) {
        this(appModel, null);
    }

    public AppModelProviderBuildItem(ApplicationModel appModel, Supplier<DependencyInfoProvider> depInfoProvider) {
        this.appModel = Objects.requireNonNull(appModel);
        this.depInfoProvider = depInfoProvider;
    }

    public ApplicationModel validateAndGet(BootstrapConfig config) {
        final PlatformImports platforms = appModel.getPlatforms();
        if (platforms != null && !BootstrapConfig.MisalignedPlatformImports.IGNORE.equals(config.misalignedPlatformImports())
                && !platforms.isAligned()) {
            switch (config.misalignedPlatformImports()) {
                case ERROR:
                    throw new RuntimeException(platforms.getMisalignmentReport());
                case WARN:
                    log.warn(platforms.getMisalignmentReport());
                    break;
                default:
                    throw new RuntimeException("Unrecognized option for quarkus.bootstrap.misaligned-platform-imports: "
                            + config.misalignedPlatformImports());
            }
        }
        return appModel;
    }

    public Supplier<DependencyInfoProvider> getDependencyInfoProvider() {
        return depInfoProvider;
    }
}
