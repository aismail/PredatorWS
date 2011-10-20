package jason.infra.saci;

import jason.infra.InfrastructureFactory;
import jason.jeditplugin.MASLauncherInfraTier;
import jason.runtime.RuntimeServicesInfraTier;

public class SaciFactory implements InfrastructureFactory {

    public MASLauncherInfraTier createMASLauncher() {
        return new SaciMASLauncherAnt();
    }

    public RuntimeServicesInfraTier createRuntimeServices() {
        return new SaciRuntimeServices(null);
    }
}
