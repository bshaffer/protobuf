/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.protobuf.kotlin.generator

import com.google.protobuf.kotlin.protoc.ClassSimpleName
import com.google.protobuf.kotlin.protoc.GeneratorConfig
import com.google.protobuf.kotlin.protoc.JavaPackagePolicy
import com.google.protobuf.kotlin.protoc.OneofComponent
import com.google.protobuf.kotlin.protoc.UnqualifiedScope
import com.google.protobuf.kotlin.protoc.classBuilder
import com.google.protobuf.kotlin.protoc.testing.assertThat
import com.google.protobuf.kotlin.generator.Example3.ExampleMessage
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Tests for [OneofCaseDslComponentGenerator]. */
@RunWith(JUnit4::class)
class OneofCaseDslComponentGeneratorTest {
  private val javaPackagePolicy = JavaPackagePolicy.OPEN_SOURCE
  private val config = GeneratorConfig(javaPackagePolicy, aggressiveInlining = false)

  @Test
  fun generate() {
    val oneofDescriptor = ExampleMessage.getDescriptor().oneofs.single { it.name == "my_oneof" }
    val oneofComponent = OneofComponent(oneofDescriptor)
    val dslSimpleName = ClassSimpleName("DslClass")
    val dslBuilder = TypeSpec.classBuilder(dslSimpleName)
    val dslCompanionBuilder = TypeSpec.companionObjectBuilder()

    with(config) {
      OneofCaseDslComponentGenerator.run {
        generate(
          component = oneofComponent,
          dslType = UnqualifiedScope.nestedClass(dslSimpleName),
          extensionScope = UnqualifiedScope,
          enclosingScope = UnqualifiedScope,
          dslBuilder = dslBuilder,
          companionBuilder = dslCompanionBuilder,
          builderCode = CodeBlock.of("myBuilder")
        )
      }
    }

    assertThat(dslBuilder.build()).generates(
      """
class DslClass {
  /**
   * An enum reflecting which field of the oneof my_oneof is set.
   */
  val myOneofCase: com.google.protobuf.kotlin.generator.Example3.ExampleMessage.MyOneofCase
    get() = myBuilder.myOneofCase
}
      """
    )
  }
}
