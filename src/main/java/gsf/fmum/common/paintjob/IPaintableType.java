package gsf.fmum.common.paintjob;

import gsf.fmum.common.Registry;

public interface IPaintableType
{
	Registry< IPaintableType > REGISTRY = new Registry<>( IPaintableType::name );
	
	String name();
	
	void injectPaintjob( IPaintjob paintjob );
}
