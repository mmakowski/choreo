package com.bimbr.choreo.activities

import android.app.Activity
import com.bimbr.choreo.app.ChoreoApplication
import com.bimbr.choreo.model.Choreography
import com.bimbr.choreo.TypedActivity

abstract class ChoreoActivity extends Activity with TypedActivity {
  protected def application: ChoreoApplication = getApplication.asInstanceOf[ChoreoApplication]

  protected def choreography: Choreography = application.getChoreography
}
