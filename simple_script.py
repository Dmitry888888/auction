#!/usr/bin/env python3
"""
Simple Python script for PyCharm
Run this with a custom configuration different from default
"""

import sys
import os
from datetime import datetime

def main():
    print("=" * 50)
    print("Custom Python Script Started")
    print("=" * 50)
    print(f"Current Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Python Version: {sys.version}")
    print(f"Working Directory: {os.getcwd()}")
    print(f"Script Path: {__file__}")
    print("=" * 50)
    
    # Example: You can add command-line argument handling
    if len(sys.argv) > 1:
        print(f"Arguments received: {sys.argv[1:]}")
    else:
        print("No command-line arguments provided")
    
    print("\nScript completed successfully!")

if __name__ == "__main__":
    main()
