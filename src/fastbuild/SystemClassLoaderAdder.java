/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fastbuild;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Uses reflection to modify the system class loader. There's no way to override or replace the
 * system class loader with our own class loader that knows how to load our pre-dexed jars. This
 * uses reflection to modify the system class loader. This was written based on careful inspection
 * of the source Android source for {@link DexClassLoader} and {@link PathClassLoader}.
 */
class SystemClassLoaderAdder {

  private SystemClassLoaderAdder() {}

  /**
   * Installs a list of .dex.jar files into the application class loader.
   *
   * @param appClassLoader The application ClassLoader, which can be retrieved by calling {@code
   *     getClassLoader} on the application Context.
   * @param dexJars The list of .dex.jar files to load.
   */
  public static void installDexJars(
      ClassLoader appClassLoader, List<File> dexJars) {
    SystemClassLoaderAdder classLoaderAdder = new SystemClassLoaderAdder();

    for (File dexJar : dexJars) {
      DexClassLoader newClassLoader =
          new DexClassLoader(
              dexJar.getAbsolutePath(), null, null, appClassLoader);
      classLoaderAdder.addPathsOfClassLoaderToSystemClassLoader(
          newClassLoader, (PathClassLoader) appClassLoader);
    }
  }

  /**
   * Adds the paths in {@code newClassLoader} to the paths in {@code systemClassLoader} using
   * reflection since there's no way to do this with public APIs.
   *
   * @param newClassLoader the class loader with the new paths
   * @param systemClassLoader the system class loader
   */
  private void addPathsOfClassLoaderToSystemClassLoader(
      DexClassLoader newClassLoader, PathClassLoader systemClassLoader) {

    try {
      addNewClassLoaderToSystemClassLoaderWithBaseDex(newClassLoader, systemClassLoader);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds the paths in {@code newClassLoader} to the paths in {@code systemClassLoader}. This works
   * with versions of Android that have {@link BaseDexClassLoader}.
   *
   * @param newClassLoader the class loader with the new paths
   * @param systemClassLoader the system class loader
   */
  private void addNewClassLoaderToSystemClassLoaderWithBaseDex(
      DexClassLoader newClassLoader, PathClassLoader systemClassLoader)
      throws NoSuchFieldException, IllegalAccessException {
    Object currentElementsArray = getDexElementsArray(getDexPathList(systemClassLoader));
    Object newElementsArray = getDexElementsArray(getDexPathList(newClassLoader));
    Object mergedElementsArray = mergeArrays(currentElementsArray, newElementsArray);
    setDexElementsArray(getDexPathList(systemClassLoader), mergedElementsArray);
  }

  private Object getDexPathList(BaseDexClassLoader classLoader)
      throws NoSuchFieldException, IllegalAccessException {
    return Reflect.getField(classLoader, BaseDexClassLoader.class, "pathList");
  }

  private Object getDexElementsArray(Object dexPathList)
      throws NoSuchFieldException, IllegalAccessException {
    return Reflect.getField(dexPathList, dexPathList.getClass(), "dexElements");
  }

  private void setDexElementsArray(Object dexPathList, Object newElementArray)
      throws NoSuchFieldException, IllegalAccessException {
    Reflect.setField(dexPathList, dexPathList.getClass(), "dexElements", newElementArray);
  }

  private Object mergeArrays(Object array1, Object array2) {
    Class<?> arrayClass = array1.getClass();
    Class<?> itemClass = arrayClass.getComponentType();
    int array1Size = Array.getLength(array1);
    int array2Size = Array.getLength(array2);
    int newSize = array1Size + array2Size;
    Object newArray = Array.newInstance(itemClass, newSize);
    for (int i = 0; i < newSize; i++) {
      if (i < array1Size) {
        Array.set(newArray, i, Array.get(array1, i));
      } else {
        Array.set(newArray, i, Array.get(array2, i - array1Size));
      }
    }
    return newArray;
  }
}
