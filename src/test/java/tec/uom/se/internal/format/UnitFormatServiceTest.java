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
package tec.uom.se.internal.format;

import static org.junit.Assert.*;

import javax.measure.spi.ServiceProvider;
import javax.measure.spi.UnitFormatService;

import org.junit.Test;

/**
 * Tests for services provided via {@link ServiceProvider}.
 */
public class UnitFormatServiceTest {

  @Test
  public void testGetServices() throws Exception {
    ServiceProvider[] services = ServiceProvider.available();
    assertNotNull(services);
    // assertFalse(services.isEmpty());
    assertEquals(1, services.length);
  }

  @Test
  public void testGetService() throws Exception {
    UnitFormatService ufs = ServiceProvider.current().getUnitFormatService();
    assertNotNull(ufs);
    assertNotNull(ufs.getUnitFormat());
    assertEquals("DefaultFormat", ufs.getUnitFormat().getClass().getSimpleName());
  }

  @Test
  public void testGetFormatFound() throws Exception {
    UnitFormatService ufs = ServiceProvider.current().getUnitFormatService();
    assertNotNull(ufs);
    assertNotNull(ufs.getUnitFormat("EBNF"));
  }

  @Test
  public void testGetFormatNotFound() throws Exception {
    UnitFormatService ufs = ServiceProvider.current().getUnitFormatService();
    assertNotNull(ufs);
    assertNull(ufs.getUnitFormat("XYZ"));
  }
}
