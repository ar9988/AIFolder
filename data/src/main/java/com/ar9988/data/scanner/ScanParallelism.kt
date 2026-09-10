package com.ar9988.data.scanner

import javax.inject.Qualifier

/**
 * 한 폴더 안의 파일을 동시에 몇 개까지 처리할지.
 *
 * 값을 바꿔가며 재보려고 주입으로 뺐다. 하드코딩돼 있으면 코드를 고쳐야 하는데,
 * 이 숫자는 기기와 저장장치에 따라 최적값이 달라서 실측으로만 정할 수 있다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ScanParallelism
