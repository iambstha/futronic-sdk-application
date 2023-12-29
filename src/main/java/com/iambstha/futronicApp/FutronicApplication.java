package com.iambstha.futronicApp;

import java.awt.GraphicsEnvironment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.futronic.SDKHelper.FutronicException;
import com.futronic.SDKHelper.FutronicSdkBase;
import com.futronicApp.workedex.MainForm;

@SpringBootApplication
public class FutronicApplication {

    public static void main(String[] args) {
        SpringApplication.run(FutronicApplication.class, args);
    }

//    @Component
//    public static class FutronicInitializer extends FutronicSdkBase implements CommandLineRunner {
//
//        public FutronicInitializer() throws FutronicException {
//			super();
//		}
//
//        @Override
//        public void run(String... args) throws Exception {
//            if (!GraphicsEnvironment.isHeadless()) {
//                initGui();
//            }
//        }
//        private void initGui() {
//            MainForm mainForm = new MainForm();
//            mainForm.pack();
//            mainForm.setVisible(true);
//            
//            System.out.println(mainForm.getX());
//            System.out.println(mainForm.getTitle());
//            
//        }
//    }
}
