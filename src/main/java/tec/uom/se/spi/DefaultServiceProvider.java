/*
 * Units of Measurement Implementation for Java SE
 * Copyright (c) 2005-2016, Jean-Marie Dautelle, Werner Keil, V2COM.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *    and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of JSR-363 nor the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tec.uom.se.spi;

import javax.annotation.Priority; // TODO this should no longer be necessary
import javax.measure.Quantity;
import javax.measure.spi.QuantityFactory;
import javax.measure.spi.QuantityFactoryService;
import javax.measure.spi.ServiceProvider;
import javax.measure.spi.SystemOfUnitsService;
import javax.measure.spi.UnitFormatService;

import tec.uom.se.quantity.DefaultQuantityFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class extends the {@link javax.measure.spi.ServiceProvider} class and
 * hereby uses the JDK {@link java.util.ServiceLoader} to load the required
 * services.
 *
 * @author Werner Keil
 * @version 0.8.6
 */
public class DefaultServiceProvider extends ServiceProvider implements Comparable<ServiceProvider> {
	/**
	 * List of services loaded, per class.
	 */
	@SuppressWarnings("rawtypes")
	private final Map<Class, List<Object>> servicesLoaded = new ConcurrentHashMap<>();

	private static final Comparator<Object> SERVICE_COMPARATOR = DefaultServiceProvider::compareServices;

	@SuppressWarnings("rawtypes")
	private final Map<Class, QuantityFactory> QUANTITY_FACTORIES = new ConcurrentHashMap<>();

	/**
	 * Returns a priority value of 10.
	 *
	 * @return 10, overriding the default provider.
	 */
	@Override
	public int getPriority() {
		return 10;
	}

	/**
	 * Loads and registers services.
	 *
	 * @param serviceType
	 *            The service type.
	 * @param <T>
	 *            the concrete type.
	 * @return the items found, never {@code null}.
	 */
	protected <T> List<T> getServices(final Class<T> serviceType) {
		@SuppressWarnings("unchecked")
		List<T> found = (List<T>) servicesLoaded.get(serviceType);
		if (found != null) {
			return found;
		}

		return loadServices(serviceType);
	}

	protected <T> T getService(Class<T> serviceType) {
		List<T> services = getServices(serviceType);
		if (services.isEmpty()) {
			return null;
		}
		return services.get(0);
	}

	static int compareServices(Object o1, Object o2) {
		int prio1 = 0;
		int prio2 = 0;
		Priority prio1Annot = o1.getClass().getAnnotation(Priority.class);
		if (prio1Annot != null) {
			prio1 = prio1Annot.value();
		}
		Priority prio2Annot = o2.getClass().getAnnotation(Priority.class);
		if (prio2Annot != null) {
			prio2 = prio2Annot.value();
		}
		if (prio1 < prio2) {
			return 1;
		}
		if (prio2 < prio1) {
			return -1;
		}
		return o2.getClass().getSimpleName().compareTo(o1.getClass().getSimpleName());
	}

	/**
	 * Loads and registers services.
	 *
	 * @param serviceType
	 *            The service type.
	 * @param <T>
	 *            the concrete type.
	 * @return the items found, never {@code null}.
	 */
	private <T> List<T> loadServices(final Class<T> serviceType) {
		List<T> services = new ArrayList<>();
		try {
			for (T t : ServiceLoader.load(serviceType)) {
				services.add(t);
			}
			Collections.sort(services, SERVICE_COMPARATOR);
			@SuppressWarnings("unchecked")
			final List<T> previousServices = (List<T>) servicesLoaded.putIfAbsent(serviceType, (List<Object>) services);
			return Collections.unmodifiableList(previousServices != null ? previousServices : services);
		} catch (Exception e) {
			Logger.getLogger(DefaultServiceProvider.class.getName()).log(Level.WARNING,
					"Error loading services of type " + serviceType, e);
			Collections.sort(services, SERVICE_COMPARATOR);
			return services;
		}
	}

	@Override
	public int compareTo(ServiceProvider o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	@Override
	public SystemOfUnitsService getSystemOfUnitsService() {
		return getService(SystemOfUnitsService.class);
	}

	@Override
	public UnitFormatService getUnitFormatService() {
		return getService(UnitFormatService.class);
	}

	@Override
	public QuantityFactoryService getQuantityFactoryService() {
		return null; // FIXME remove after API change
	}

	 /**
	   * Return a factory for this quantity
	   * 
	   * @param quantity
	   *          the quantity type
	   * @return the {@link QuantityFactory}
	   * @throws NullPointerException
	   */
	  @Override
	  @SuppressWarnings("unchecked")
	  public final <Q extends Quantity<Q>> QuantityFactory<Q> getQuantityFactory(Class<Q> quantity) {
	    if (quantity == null)
	      throw new NullPointerException();
	    if (!QUANTITY_FACTORIES.containsKey(quantity)) {
	      synchronized (QUANTITY_FACTORIES) {
	        QUANTITY_FACTORIES.put(quantity, DefaultQuantityFactory.getInstance(quantity));
	      }
	    }
	    return QUANTITY_FACTORIES.get(quantity);
	  }
}