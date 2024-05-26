Read this please for Gradle Scripts Update. Also, pede mo update diri sa readme if naa moy usbon sa gradle. So, far  for now mao ni ang dapat naa sa inyong Gradle Scrips


//build.gradle.kts (Project: WoodablesApp)

buildscript {

    dependencies {
    
        classpath("com.google.gms:google-services:4.4.1")
    }
}




//build.gradle.kts (Module :app)

plugins {

    id("com.android.application")
    id("com.google.gms.google-services")
}



dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.gms:play-services-auth:20.0.0")
    implementation  ("com.google.firebase:firebase-bom:33.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.firebase:firebase-analytics:22.0.0")
    implementation ("com.google.firebase:firebase-firestore:25.0.0")
    implementation ("com.google.firebase:firebase-storage:21.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
}
