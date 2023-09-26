package com.di.berdi

import android.content.Context
import com.di.berdi.annotation.Singleton
import com.di.berdi.util.declaredInjectProperties
import com.di.berdi.util.qualifiedName
import com.di.berdi.util.setInstance
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

class Injector(private val container: Container, private val module: Module) {

    // 생성자를 받아서 생성한다
    fun <T : Any> createBy(context: Context, constructor: KFunction<T>): T {
        val paramInstances = getInstancesParamsOf(context, constructor)
        return constructor.call(*paramInstances.toTypedArray())
            .apply { injectProperties(context, this) }
    }

    fun injectProperties(context: Context, target: Any) {
        val properties = target::class.declaredInjectProperties
        properties.forEach { property -> inject(context, property, target) }
    }

    // 파라미터의 인스턴스를 다 가져온다
    private fun getInstancesParamsOf(context: Context, callable: KCallable<*>): List<Any?> {
        return callable.parameters.map { param ->
            getInstanceOf(context, param.type, param.qualifiedName)
        }
    }

    // 인스턴스를 가져온다
    private fun getInstanceOf(context: Context, type: KType, qualifiedName: String?): Any {
        // 해당 param 타입의 인스턴스를 Container 에서 가져온다
        container.getInstance(type.jvmErasure, qualifiedName)?.let { return it }

        // 없으면 생성한다
        return createInstance(context, type, qualifiedName)
    }

    private fun createInstance(context: Context, type: KType, qualifiedName: String?): Any {
        // 모듈에서 파람과 맞는 타입을 찾는다
        val targetProvider = requireNotNull(
            module::class.declaredFunctions.find { providers ->
                isTargetProvider(providers, type, qualifiedName)
            },
        ) { ERROR_NOT_FOUND_MATCHED_MODULE }

        // 파라미터가 없다면 만든다
        if (targetProvider.valueParameters.isEmpty()) {
            return requireNotNull(targetProvider.call(module)).also {
                storeInstance(targetProvider, it)
            }
        }

        // 파라미터를 하나씩 채운다
        val params = targetProvider.valueParameters.map { param ->
            when {
                param.type.jvmErasure == Context::class && targetProvider.hasAnnotation<Singleton>() -> context.applicationContext
                param.type.jvmErasure == Context::class -> context
                else -> getInstanceOf(context, param.type, param.qualifiedName)
            }
        }

        // 새 인스턴스 생성
        val instance = requireNotNull(targetProvider.call(module, *params.toTypedArray()))
        return instance.also { storeInstance(targetProvider, it) }
    }

    private fun isTargetProvider(
        provider: KFunction<*>,
        type: KType,
        qualifiedName: String?,
    ): Boolean {
        if (qualifiedName != null) {
            return provider.returnType == type && provider.qualifiedName == qualifiedName
        }
        return provider.returnType == type
    }

    private fun storeInstance(param: KFunction<*>, instance: Any) {
        if (param.hasAnnotation<Singleton>()) {
            container.setInstance(instance, param.returnType.jvmErasure, param.qualifiedName)
        }
    }

    // 프로퍼티에 맞는 타입을 찾아 주입한다
    private fun inject(context: Context, property: KProperty<*>, target: Any) {
        val paramInstances = getInstanceOf(context, property.returnType, property.qualifiedName)
        property.javaField?.setInstance(target, paramInstances)
    }

    companion object {
        private const val ERROR_NOT_FOUND_MATCHED_MODULE = "모듈에 맞는 매치되는 인스턴스를 찾을 수 없습니다"
    }
}
