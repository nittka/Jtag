/*******************************************************************************
 * Copyright (C) 2017-2020 Alexander Nittka (alex@nittka.de)
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package de.nittka.tooling.jtag.scoping

import de.nittka.tooling.jtag.jtag.Category
import de.nittka.tooling.jtag.jtag.CategoryRef
import de.nittka.tooling.jtag.jtag.ShortCut
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

/**
 * This class contains custom scoping description.
 * 
 * see : http://www.eclipse.org/Xtext/documentation.html#scoping
 * on how and when to use it 
 *
 */
class JtagScopeProvider extends AbstractDeclarativeScopeProvider {

	def IScope scope_CategoryRef_categories(CategoryRef context, EReference ref){
		return Scopes.scopeFor(EcoreUtil2.getAllContentsOfType(context.type, Category));
	}

	def IScope scope_ShortCut_category(ShortCut context, EReference ref){
		return Scopes.scopeFor(EcoreUtil2.getAllContentsOfType(context.type, Category));
	}

}
