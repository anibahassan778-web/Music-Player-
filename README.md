# تطبيق مشغل الموسيقى (Music Player)

تطبيق مشغل موسيقى متكامل لأندرويد مكتوب بلغة **Kotlin** و **Jetpack Compose** ومبني باستخدام **Media3 ExoPlayer**، ويدعم البناء المباشر محليًا أو عبر منصة **Codemagic**.

---

## 🚀 طريقة الرفع إلى GitHub والبناء على Codemagic

### 1. الرفع إلى GitHub
افتح موجه الأوامر في مجلد المشروع ونفّذ الأوامر التالية:

```bash
git init
git add .
git commit -m "Initial commit: Android Music Player"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

### 2. البناء التلقائي على Codemagic
1. سجّل الدخول إلى [Codemagic](https://codemagic.io) واربط حساب GitHub الخاص بك.
2. اختر مستودع المشروع (Repository).
3. سيتعرف Codemagic تلقائيًا على ملف `codemagic.yaml` الموجود في جذر المشروع.
4. اضغط على **Start new build** واختر سير العمل `android-build`.
5. سيبدأ البناء تلقائيًا باستخدام أمر:
   ```bash
   chmod +x ./gradlew
   ./gradlew assembleDebug
   ```

### 📦 مسار ملف الـ APK الناتج:
بعد انتهاء البناء، ستجد ملف الـ APK جاهزًا للتحميل في:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## ✨ ميزات التطبيق
- **قراءة المقاطع الصوتية**: قراءة تلقائية من ذاكرة الجهاز عبر `MediaStore`.
- **أذونات أندرويد الحديثة**: دعم كامل لإذن `READ_MEDIA_AUDIO` لأندرويد 13 فما فوق و `READ_EXTERNAL_STORAGE` للإصدارات الأقدم.
- **تشغيل في الخلفية**: عبر خدمة `MusicService : MediaSessionService` مع إشعار تحكم وتكامل شاشة القفل.
- **التعامل مع انقطاع الصوت**: معالجة تلقائية لـ `AudioFocus` و `AudioBecomingNoisy` (إيقاف مؤقت عند فصل السماعات).
- **قاعدة بيانات Room**: حفظ الأغاني المفضلة وقوائم التشغيل المخصصة (إنشاء، إعادة تسمية، إضافة، حذف).
- **مشغل مصغر وكامل**: شريط تحكم عائم أسفل الشاشة + شاشة تشغيل كاملة مع شريط تقدم قابل للسحب.
- **أوضاع التشغيل**: عشوائي (Shuffle)، تكرار الكل (Repeat All)، تكرار أغنية (Repeat One).
- **مؤقت النوم (Sleep Timer)**: مؤقت مرن لإيقاف التشغيل تلقائيًا بعد وقت محدد.
- **استئناف التشغيل**: حفظ واستعادة موضع آخر أغنية عبر DataStore.
- **تصميم Material 3**: دعم كامل للوضعين الفاتح والداكن، ودعم كامل للغة العربية والاتجاه من اليمين إلى اليسار (RTL).
