package io.github.nullptrx.pangleflutter.delegate

import android.app.Activity
import android.view.View
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import io.github.nullptrx.pangleflutter.common.PangleEventStreamHandler
import io.github.nullptrx.pangleflutter.common.kBlock

class FLTInterstitialExpressAd(var target: Activity?, var result: (Any) -> Unit) :
  TTAdNative.NativeExpressAdListener, TTAdDislike.DislikeInteractionCallback,
  TTNativeExpressAd.AdInteractionListener {

  private var ttNativeAd: TTNativeExpressAd? = null


  override fun onNativeExpressAdLoad(ttNativeExpressAds: MutableList<TTNativeExpressAd>?) {
    PangleEventStreamHandler.interstitial("load")
    target?.also {
      if (ttNativeExpressAds?.size ?: 0 > 0) {
        val ttNativeAd = ttNativeExpressAds!![0]
        ttNativeAd.setDislikeCallback(it, this)
        ttNativeAd.setExpressInteractionListener(this)
        ttNativeAd.render()
        this.ttNativeAd = ttNativeAd
      }
    }
  }

  override fun onError(code: Int, message: String?) {
    PangleEventStreamHandler.interstitial("error")
    invoke(code, message)
  }

  // ###  DISLIKE START  ###
  override fun onShow() {
    PangleEventStreamHandler.interstitial("dislike_show")
  }

  override fun onSelected(index: Int, selection: String?, fromUser: Boolean) {
    PangleEventStreamHandler.interstitial("dislike_selected")
  }

  override fun onCancel() {
    PangleEventStreamHandler.interstitial("dislike_cancel")
  }
  // ###  DISLIKE END  ###

  override fun onAdDismiss() {
    PangleEventStreamHandler.interstitial("dismiss")
    try {
      ttNativeAd?.destroy()
      ttNativeAd = null
    } catch (e: Exception) {
    }
    invoke()
  }

  override fun onAdClicked(view: View, type: Int) {
    PangleEventStreamHandler.interstitial("click")
  }

  override fun onAdShow(view: View?, type: Int) {
    PangleEventStreamHandler.interstitial("show")
  }

  override fun onRenderSuccess(view: View, width: Float, height: Float) {
    PangleEventStreamHandler.interstitial("render_success")
    target?.also {
      ttNativeAd?.showInteractionExpressAd(it)
    }

  }

  override fun onRenderFail(view: View?, msg: String?, code: Int) {
    PangleEventStreamHandler.interstitial("render_fail")
    invoke(code, msg)
  }


  private fun invoke(code: Int = 0, message: String? = null) {
    if (result == kBlock) {
      return
    }
    result.apply {
      val args = mutableMapOf<String, Any?>()
      args["code"] = code
      message?.also {
        args["message"] = it
      }
      invoke(args)
      result = kBlock
    }
    target = null
  }

}