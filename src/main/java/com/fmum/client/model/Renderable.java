package com.fmum.client.model;

import com.fmum.client.EventHandlerClient;

public interface Renderable
{
	public default float smoother() { return EventHandlerClient.renderTickTime; }
}
